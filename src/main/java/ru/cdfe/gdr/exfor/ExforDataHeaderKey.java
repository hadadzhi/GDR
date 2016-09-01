package ru.cdfe.gdr.exfor;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@Embeddable
@MappedSuperclass
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class ExforDataHeaderKey implements Serializable {
	@Column(name = "subent")
	private String subEntNumber;
	
	@Column(name = "col")
	private int column;
	
	@Column(name = "isc")
	private String isc;
}
