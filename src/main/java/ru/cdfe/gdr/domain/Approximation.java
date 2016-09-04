package ru.cdfe.gdr.domain;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import ru.cdfe.gdr.validation.Finite;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Approximation {
	@NotBlank
	private String description;
	
	@Finite
	private double chiSquaredWeighted;
	
	@Finite
	private double chiSquaredUnweighted;
	
	@Valid
	private List<DataPoint> sourceData;
	
	public List<DataPoint> getSourceData() {
		if (sourceData == null) {
			sourceData = new ArrayList<>();
		}
		
		return sourceData;
	}
	
	@NotEmpty
	@Valid
	private List<Curve> curves;
	
	public List<Curve> getCurves() {
		if (curves == null) {
			curves = new ArrayList<>();
		}
		
		return curves;
	}
}
