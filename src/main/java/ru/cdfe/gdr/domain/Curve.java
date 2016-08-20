package ru.cdfe.gdr.domain;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class Curve {
	@NotBlank
	private String type;
	
	@NotNull
	@Valid
	private Quantity maxCrossSection;
	
	@NotNull
	@Valid
	private Quantity energyAtMaxCrossSection;
	
	@NotNull
	@Valid
	private Quantity fullWidthAtHalfMaximum;
}
