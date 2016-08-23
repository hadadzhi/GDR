package ru.cdfe.gdr.domain;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import ru.cdfe.gdr.validation.Finite;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Quantity {
	public static final String NO_DIM = "NO-DIM";
	
	@NotNull
	@Finite
	private Double value;
	
	@Finite
	private Double error;
	
	@NotBlank
	private String dimension;
	
	public Quantity(Double value, Double error) {
		this(value, error, NO_DIM);
	}
	
	public Quantity(Double value, Double error, String dimension) {
		this.value = value;
		this.error = error;
		this.dimension = dimension;
	}
}
