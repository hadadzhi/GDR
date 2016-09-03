package ru.cdfe.gdr.domain;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import ru.cdfe.gdr.validation.Finite;

import javax.validation.Valid;
import java.util.Collections;
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
		if (sourceData != null) {
			return Collections.unmodifiableList(sourceData);
		} else {
			return Collections.emptyList();
		}
	}
	
	@NotEmpty
	@Valid
	private List<Curve> curves;
	
	public List<Curve> getCurves() {
		if (curves != null) {
			return Collections.unmodifiableList(curves);
		} else {
			return Collections.emptyList();
		}
	}
}
