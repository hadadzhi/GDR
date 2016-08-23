package ru.cdfe.gdr.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Reaction {
	@NotBlank
	private String incident;
	
	@NotBlank
	private String outgoing;
	
	public Reaction(String incident, String outgoing) {
		this.incident = incident;
		this.outgoing = outgoing;
	}
}
