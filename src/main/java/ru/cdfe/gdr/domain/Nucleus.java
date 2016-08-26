package ru.cdfe.gdr.domain;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PACKAGE)
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
	
	public Nucleus(Integer charge, Integer mass, String symbol) {
		this.charge = charge;
		this.mass = mass;
		this.symbol = symbol;
	}
}
