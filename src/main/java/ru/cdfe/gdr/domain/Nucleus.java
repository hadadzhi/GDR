package ru.cdfe.gdr.domain;

import lombok.Data;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class Nucleus {
	public static final String NO_SYMBOL = "";
	
	@NotNull
	@Min(1)
	private Integer charge;
	
	@NotNull
	@Min(1)
	private Integer mass;
	
	@NotNull // TODO not blank?
	private String symbol;
	
	public Nucleus(Integer charge, Integer mass) {
		this(charge, mass, NO_SYMBOL);
	}
	
	@PersistenceConstructor
	public Nucleus(Integer charge, Integer mass, String symbol) {
		this.charge = charge;
		this.mass = mass;
		this.symbol = symbol;
	}
}
