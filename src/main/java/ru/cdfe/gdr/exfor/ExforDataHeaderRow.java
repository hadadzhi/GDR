package ru.cdfe.gdr.exfor;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "dhead")
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExforDataHeaderRow {
	@Column(name = "subent")
	private String subEntNumber;
	
	@Column(name = "col")
	private int column;
	
	@Column(name = "head")
	private String name;
	
	@Column(name = "unit")
	private String dimension;
}
