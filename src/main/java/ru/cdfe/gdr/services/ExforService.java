package ru.cdfe.gdr.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
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
			"FROM ddata JOIN dhead ON ddata.col = dhead.col AND ddata.subent = dhead.subent WHERE ddata.subent = ?";
		
		final List<DataPoint> result = jdbc
			.query(query, (rs, row) -> new DBRow(rs.getInt(1), rs.getInt(2), rs.getDouble(3), rs.getString(4)), subEntNumber)
			.stream()
			.collect(groupingBy(DBRow::getRow))
			.values()
			.stream()
			.map(exforRow -> makeDataPoint(exforRow, energyColumn, csColumn, csErrorColumn, subEntNumber))
			.collect(toList());
		
		if (result.isEmpty()) {
			throw new NoExforDataException(subEntNumber);
		}
		
		postProcessData(result);
		
		return result;
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
		final List<Double> energies = data.stream().map(p -> p.getEnergy().getValue()).collect(toList());
		final List<Double> crossSections = data.stream().map(p -> p.getCrossSection().getValue()).collect(toList());
		final List<Double> crossSectionErrors = data.stream().map(p -> p.getCrossSection().getError()).collect(toList());
		
		exforInterpolate(energies);
		exforInterpolate(crossSections);
		exforInterpolate(crossSectionErrors);

		for (int i = 0; i < data.size(); i++) {
			final DataPoint current = data.get(i);
			current.getEnergy().setValue(energies.get(i));
			current.getCrossSection().setValue(crossSections.get(i));
			current.getCrossSection().setError(crossSectionErrors.get(i));
		}
	}
	
	private static void exforInterpolate(List<Double> elements) {
		// Leading/trailing zeros are replaced with first/last non-zero element
		int head;
		for (head = 0; head < elements.size(); head++) {
			if (!fpZero(elements.get(head))) {
				break;
			}
		}
		
		int tail;
		for (tail = elements.size() - 1; tail >= 0; tail--) {
			if (!fpZero(elements.get(tail))) {
				break;
			}
		}
		
		final double first = elements.get(head);
		final double last = elements.get(tail);
		
		for (int i = 0; i < head; i++) {
			elements.set(i, first);
		}
		
		for (int i = tail; i < elements.size(); i++) {
			elements.set(i, last);
		}
		
		// Zeros between non-zero elements are replaced with the result of linear interpolation
		double prev = 0;
		double next = 0;
		int prevIndex = head;
		int nextIndex = tail;
		for (int i = head; i < tail; i++) {
			final double current = elements.get(i);
			if (!fpZero(current)) {
				prev = current;
				prevIndex = i;
				if (fpEqual(next, current)) {
					next = 0;
				}
			} else {
				if (fpZero(next)) {
					nextIndex = i;
					while (fpZero(elements.get(nextIndex))) {
						nextIndex++;
					}
					next = elements.get(nextIndex);
				}
				elements.set(i, prev + (next - prev) * (i - prevIndex) / (nextIndex - prevIndex));
			}
		}
	}
	
	private static boolean fpZero(double a) {
		return fpEqual(a, 0.);
	}
	
	private static boolean fpEqual(double a, double b) {
		return Math.abs(a - b) < 1E-38; // Minimum non-zero value allowed by EXFOR
	}
	
	@Getter
	@ToString
	@RequiredArgsConstructor
	static final class DBRow {
		private final int row;
		private final int col;
		private final double val;
		private final String dim;
	}
}

@Component
@Slf4j
class TestExforService implements ApplicationRunner {
	private final ExforService exforService;
	
	@Autowired
	public TestExforService(ExforService exforService) {
		this.exforService = exforService;
	}
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		final long start = System.currentTimeMillis();
		exforService.getData("M0040002", 0, 1, 2).forEach(p -> log.info(p.toString()));
		final long end = System.currentTimeMillis() - start;
		log.info(Long.toString(end));
	}
}
