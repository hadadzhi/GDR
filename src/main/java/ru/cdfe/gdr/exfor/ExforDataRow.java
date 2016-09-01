package ru.cdfe.gdr.exfor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ddata")
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExforDataRow {
	@Column(name = "subent")
	private String subEntNumber;

	@Column(name = "col")
	private int column;
	
	@Column(name = "row")
	private int row;
	
	@Column(name = "dt")
	private double data;
}
