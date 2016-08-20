package ru.cdfe.gdr.domain;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.PersistenceConstructor;

@Data
public class Reaction {
	@NotBlank
	private String incident;
	
	@NotBlank
	private String outgoing;
	
	@PersistenceConstructor
	public Reaction(String incident, String outgoing) {
		this.incident = incident;
		this.outgoing = outgoing;
	}
}
