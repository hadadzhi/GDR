package ru.cdfe.gdr.web.representations;

import lombok.Getter;
import ru.cdfe.gdr.domain.Approximation;
import ru.cdfe.gdr.domain.Curve;
import ru.cdfe.gdr.domain.DataPoint;

import java.util.List;

@Getter
public class ApproximationProjection {
	public static final String RELATION = "approximation";
	public static final String COLLECTION_RELATION = "approximations";

	private final String description;
	private final Double chiSquaredUnweighted;
	private final Double chiSquaredWeighted;
	
	private final List<DataPoint> sourceData;
	private final List<Curve> curves;

	public ApproximationProjection(Approximation approximation) {
		this.description = approximation.getDescription();
		this.chiSquaredUnweighted = approximation.getChiSquaredUnweighted();
		this.chiSquaredWeighted = approximation.getChiSquaredWeighted();
		
		this.sourceData = approximation.getSourceData();
		this.curves = approximation.getCurves();
	}
}
