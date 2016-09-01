package ru.cdfe.gdr.exfor;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "dhead")
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class ExforDataHeaderRow {
	@EmbeddedId
	private ExforDataHeaderKey key;
	
	@Column(name = "head")
	private String name;
	
	@Column(name = "unit")
	private String dimension;
}
