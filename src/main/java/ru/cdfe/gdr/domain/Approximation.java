package ru.cdfe.gdr.domain;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import ru.cdfe.gdr.validation.Finite;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@Data
@Builder
public class Approximation {
	@NotBlank
	private String description;

	@Finite // TODO and non-null?
	private Double chiSquaredWeighted;
	
	@Finite // TODO and non-null?
	private Double chiSquaredUnweighted;
	
	@Valid
	private List<DataPoint> sourceData;

	public List<DataPoint> getSourceData() {
		return Collections.unmodifiableList(sourceData);
	}

	@NotEmpty
	@Valid
	private List<Curve> curves;
	
	public List<Curve> getCurves() {
		return Collections.unmodifiableList(curves);
	}
}
