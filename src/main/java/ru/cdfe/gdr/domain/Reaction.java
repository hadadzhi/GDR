package ru.cdfe.gdr.domain;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

@Getter
@Setter
@ToString
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
