package ru.cdfe.gdr.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.cdfe.gdr.domain.DataPoint;
import ru.cdfe.gdr.domain.Quantity;
import ru.cdfe.gdr.exceptions.BadExforDataException;
import ru.cdfe.gdr.exceptions.NoExforDataException;
import ru.cdfe.gdr.exceptions.NoSuchColumnException;

import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static ru.cdfe.gdr.constants.Profiles.OPERATOR;

@Service
@Profile(OPERATOR)
@Slf4j
public class ExforService {
	private final JdbcTemplate jdbc;
	
	@Autowired
	public ExforService(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}
	
	public List<DataPoint> getData(String subEntNumber, int energyColumn, int csColumn, int csErrorColumn) {
		final String query =
			"SELECT ddata.row, ddata.col, ddata.dt, dhead.unit\n" +
			"FROM ddata\n" +
			"JOIN dhead ON ddata.col = dhead.col AND ddata.subent = dhead.subent AND ddata.isc = dhead.isc\n" +
			"WHERE ddata.isc = 'd' AND ddata.subent = ?";
		
		final List<DataPoint> data = jdbc
			.query(query, (rs, row) -> new DBRow(rs.getInt(1), rs.getInt(2), rs.getDouble(3), rs.getString(4)), subEntNumber)
			.stream()
			.collect(groupingBy(DBRow::getRow))
			.values()
			.stream()
			.map(exforRow -> makeDataPoint(exforRow, energyColumn, csColumn, csErrorColumn, subEntNumber))
			.collect(toList());
		
		if (data.isEmpty()) {
			throw new NoExforDataException(subEntNumber);
		}
		
		postProcessData(data);
		
		return data;
	}
	
	private static DataPoint makeDataPoint(List<DBRow> exforRow, int energyColumn, int csColumn, int csErrorColumn, String subEntNumber) {
		final DBRow energy = extractColumn(exforRow, energyColumn, subEntNumber);
		final DBRow crossSection = extractColumn(exforRow, csColumn, subEntNumber);
		final DBRow crossSectionError = extractColumn(exforRow, csErrorColumn, subEntNumber);
		
		if (!crossSection.getDim().equals(crossSectionError.getDim())) {
			throw new BadExforDataException("Cross section value dimension does not match cross section error dimension", subEntNumber);
		}
		
		return new DataPoint(
			new Quantity(energy.getVal(), energy.getDim()),
			new Quantity(crossSection.getVal(), crossSectionError.getVal(), crossSection.getDim()));
	}
	
	private static DBRow extractColumn(List<DBRow> exforRow, int column, String subEntNumber) {
		return exforRow.stream()
			.filter(r -> r.getCol() == column)
			.findAny().orElseThrow(() -> new NoSuchColumnException(column, subEntNumber));
	}
	
	private static void postProcessData(List<DataPoint> data) {
		exforInterpolate(i -> data.get(i).getEnergy().getValue(), (i, v) -> data.get(i).getEnergy().setValue(v), data.size());
		exforInterpolate(i -> data.get(i).getCrossSection().getValue(), (i, v) -> data.get(i).getCrossSection().setValue(v), data.size());
		exforInterpolate(i -> data.get(i).getCrossSection().getError(), (i, v) -> data.get(i).getCrossSection().setError(v), data.size());
	}
	
	@FunctionalInterface
	private interface DoubleListGetter {
		double get(int index);
	}
	
	@FunctionalInterface
	private interface DoubleListSetter {
		void set(int index, double value);
	}
	
	private static void exforInterpolate(DoubleListGetter getter, DoubleListSetter setter, int size) {
		// Leading/trailing zeros are replaced with first/last non-zero element
		int head; // Index of the first non-zero element
		for (head = 0; head < size; head++) {
			if (!nearZero(getter.get(head))) {
				break;
			}
		}
		
		int tail; // Index of the last non-zero element
		for (tail = size - 1; tail >= 0; tail--) {
			if (!nearZero(getter.get(tail))) {
				break;
			}
		}
		
		final double first = getter.get(head);
		final double last = getter.get(tail);
		
		for (int i = 0; i < head; i++) {
			setter.set(i, first);
		}
		
		for (int i = tail; i < size; i++) {
			setter.set(i, last);
		}
		
		// Zero elements between non-zero elements are replaced with the result of linear interpolation
		int prevPos = head;
		double prev = getter.get(head);
		for (int i = head + 1; i < tail; i++) {
			final double current = getter.get(i);
			if (!nearZero(current)) {
				prev = current;
				prevPos = i;
			} else {
				int nextPos = i + 1;
				while (nearZero(getter.get(nextPos))) {
					nextPos++;
				}
				double next = getter.get(nextPos);
				setter.set(i, prev + (((next - prev) * (i - prevPos)) / (nextPos - prevPos)));
			}
		}
	}
	
	private static boolean nearZero(double a) {
		return Math.abs(a) < 1e-38; // Minimum non-zero absolute value allowed by EXFOR
	}
	
	@Getter
	@RequiredArgsConstructor
	static final class DBRow {
		private final int row;
		private final int col;
		private final double val;
		private final String dim;
	}
	
	@Component
	static class TestExforService implements ApplicationRunner {
		private final ExforService exforService;
		
		@Autowired
		public TestExforService(ExforService exforService) {
			this.exforService = exforService;
		}
		
		@Override
		public void run(ApplicationArguments args) throws Exception {
			exforService.getData("M0040004", 0, 1, 2).forEach(p -> log.info(p.toString()));
		}
	}
}
