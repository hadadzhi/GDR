package ru.cdfe.gdr.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
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
	
	@PersistenceConstructor
	public Nucleus(Integer charge, Integer mass, String symbol) {
		this.charge = charge;
		this.mass = mass;
		this.symbol = symbol;
	}
}
