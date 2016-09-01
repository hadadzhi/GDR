package ru.cdfe.gdr.exfor;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ddata")
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class ExforDataRow {
	@EmbeddedId
	private ExforDataKey key;
	
	@Column(name = "dt")
	private double data;
}
