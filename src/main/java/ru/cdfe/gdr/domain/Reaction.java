package ru.cdfe.gdr.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.PersistenceConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PACKAGE)
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
