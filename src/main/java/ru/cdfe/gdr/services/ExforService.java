package ru.cdfe.gdr.services;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.cdfe.gdr.domain.DataPoint;
import ru.cdfe.gdr.domain.Quantity;

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
	
	public List<DataPoint> extractSourceData(String subEntNumber, int energyColumn, int csColumn, int csErrorColumn) {
		final String query = "\n" +
			"SELECT ddata.row, ddata.col, dhead.unit, ddata.dt\n" +
			"FROM ddata JOIN dhead ON ddata.col = dhead.col AND ddata.subent = dhead.subent WHERE ddata.subent = ?";
		
		return jdbc.query(query, (rs, row) -> new ExforDataRow(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getDouble(4)), subEntNumber)
			.stream()
			.collect(groupingBy(ExforDataRow::getRow, groupingBy(ExforDataRow::getCol)))
			.values()
			.stream()
			.map(col -> new DataPoint(
				new Quantity(col.get(energyColumn).get(0).getVal(), col.get(energyColumn).get(0).getDim()),
				new Quantity(col.get(csColumn).get(0).getVal(), col.get(csErrorColumn).get(0).getVal(), col.get(csColumn).get(0).getDim())))
			.collect(toList());
	}
	
	@Getter
	@ToString
	static final class ExforDataRow {
		private final int row;
		private final int col;
		private final String dim;
		private final double val;
		
		public ExforDataRow(int row, int col, String dim, double val) {
			this.row = row;
			this.col = col;
			this.dim = dim;
			this.val = val;
		}
	}
}
