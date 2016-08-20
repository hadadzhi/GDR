package ru.cdfe.gdr.domain;

import lombok.Data;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
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
