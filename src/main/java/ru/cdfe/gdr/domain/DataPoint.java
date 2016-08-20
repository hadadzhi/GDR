package ru.cdfe.gdr.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class DataPoint {
	@NotNull
	@Valid
	private Quantity energy;
	
	@NotNull
	@Valid
	private Quantity crossSection;

	@PersistenceConstructor
	public DataPoint(Quantity energy, Quantity crossSection) {
		this.energy = energy;
		this.crossSection = crossSection;
	}
}
