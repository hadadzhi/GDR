package ru.cdfe.gdr.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import ru.cdfe.gdr.domain.DataPoint;
import ru.cdfe.gdr.domain.Nucleus;
import ru.cdfe.gdr.domain.Quantity;
import ru.cdfe.gdr.domain.Reaction;
import ru.cdfe.gdr.exceptions.BadExforDataException;
import ru.cdfe.gdr.exceptions.NoExforDataException;
import ru.cdfe.gdr.exceptions.NoSuchColumnException;

import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static ru.cdfe.gdr.constants.Profiles.OPERATOR;

@Service
@Profile(OPERATOR)
public class ExforService {
	private final JdbcTemplate jdbc;
	
	@Autowired
	public ExforService(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}
	
	public List<DataPoint> getData(String subEntNumber, int energyColumn, int crossSectionColumn, int crossSectionErrorColumn) {
		return new DataRetriever(jdbc, subEntNumber, energyColumn, crossSectionColumn, crossSectionErrorColumn).retrieveData();
	}
	
	public List<Reaction> getReactions(String subEntNumber) {
		return new ReactionRetriever(jdbc, subEntNumber).retrieveReactions();
	}
	
	private static class ReactionRetriever {
		private final JdbcTemplate jdbc;
		private final String subEntNumber;
		
		private ReactionRetriever(JdbcTemplate jdbc, String subEntNumber) {
			this.jdbc = jdbc;
			this.subEntNumber = subEntNumber;
		}
		
		private List<Reaction> retrieveReactions() {
			final String query = "\n" +
				"SELECT react1.tz, react1.ta, react1.pz, react1.pa, react1.inc, react1.`out`\n" +
				"FROM react1\n" +
				"WHERE react1.subent = ?";
			
			final RowMapper<Reaction> rowMapper = (rs, row) -> Reaction.builder()
				.target(new Nucleus(rs.getInt("tz"), rs.getInt("ta")))
				.product(new Nucleus(rs.getInt("pz"), rs.getInt("pa")))
				.incident(rs.getString("inc"))
				.outgoing(rs.getString("out"))
				.build();
			
			return jdbc
				.query(query, rowMapper, subEntNumber)
				.stream()
				.collect(toList());
		}
	}
	
	private static class DataRetriever {
		private final JdbcTemplate jdbc;
		private final String subEntNumber;
		private final int energyColumn;
		private final int crossSectionColumn;
		private final int crossSectionErrorColumn;
		
		private DataRetriever(JdbcTemplate jdbc, String subEntNumber, int energyColumn, int crossSectionColumn, int crossSectionErrorColumn) {
			this.jdbc = jdbc;
			this.subEntNumber = subEntNumber;
			this.energyColumn = energyColumn;
			this.crossSectionColumn = crossSectionColumn;
			this.crossSectionErrorColumn = crossSectionErrorColumn;
		}
		
		private List<DataPoint> retrieveData() {
			final String query = "\n" +
				"SELECT ddata.row, ddata.col, ddata.dt, dhead.unit\n" +
				"FROM ddata\n" +
				"JOIN dhead ON ddata.col = dhead.col AND ddata.subent = dhead.subent AND ddata.isc = dhead.isc\n" +
				"WHERE ddata.isc = 'd' AND ddata.subent = ?";
			
			final RowMapper<DBRow> rowMapper = (rs, row) -> new DBRow(rs.getInt("row"), rs.getInt("col"), rs.getDouble("dt"), rs.getString("unit"));
			
			final List<DataPoint> data = jdbc
				.query(query, rowMapper, subEntNumber)
				.stream()
				.collect(groupingBy(DBRow::getRow))
				.values()
				.stream()
				.map(this::makeDataPoint)
				.collect(toList());
			
			if (data.isEmpty()) {
				throw new NoExforDataException(subEntNumber);
			}
			
			postProcessData(data);
			
			return data;
		}
		
		private DataPoint makeDataPoint(List<DBRow> exforRow) {
			final DBRow energy = extractColumn(exforRow, energyColumn);
			final DBRow crossSection = extractColumn(exforRow, crossSectionColumn);
			final DBRow crossSectionError = extractColumn(exforRow, crossSectionErrorColumn);
			
			if (!crossSection.getDim().equals(crossSectionError.getDim())) {
				throw new BadExforDataException("Cross section value dimension does not match cross section error dimension", subEntNumber);
			}
			
			return new DataPoint(
				new Quantity(energy.getVal(), energy.getDim()),
				new Quantity(crossSection.getVal(), crossSectionError.getVal(), crossSection.getDim())
			);
		}
		
		private DBRow extractColumn(List<DBRow> exforRow, int column) {
			return exforRow.stream()
				.filter(r -> r.getCol() == column)
				.findAny().orElseThrow(() -> new NoSuchColumnException(column, subEntNumber));
		}
		
		private void postProcessData(List<DataPoint> data) {
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
		
		private void exforInterpolate(DoubleListGetter getter, DoubleListSetter setter, int size) {
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
			int nextPos = prevPos;
			double prev = getter.get(prevPos);
			double next = prev;
			for (int i = head + 1; i < tail; i++) {
				final double current = getter.get(i);
				if (!nearZero(current)) {
					prev = current;
					prevPos = i;
				} else {
					if (prevPos >= nextPos) {
						nextPos = i + 1;
						while (nearZero(getter.get(nextPos))) {
							nextPos++;
						}
						next = getter.get(nextPos);
					}
					setter.set(i, prev + (((next - prev) * (i - prevPos)) / (nextPos - prevPos)));
				}
			}
		}
		
		private boolean nearZero(double a) {
			return Math.abs(a) < 1e-38; // Minimum non-zero absolute value allowed by EXFOR
		}
		
		@Getter
		private static final class DBRow {
			private final int row;
			private final int col;
			private final double val;
			private final String dim;
			
			private DBRow(int row, int col, double val, String dim) {
				this.row = row;
				this.col = col;
				this.val = val;
				this.dim = dim;
			}
		}
	}
}
