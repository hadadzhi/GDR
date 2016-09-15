package ru.cdfe.gdr.services;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.freehep.math.minuit.FCNBase;
import org.freehep.math.minuit.FunctionMinimum;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnUserParameters;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.cdfe.gdr.domain.Approximation;
import ru.cdfe.gdr.domain.Curve;
import ru.cdfe.gdr.domain.DataPoint;
import ru.cdfe.gdr.exceptions.FittingException;

import java.util.List;
import java.util.UUID;

import static ru.cdfe.gdr.constants.Profiles.OPERATOR;

@Slf4j
@Service
@Profile(OPERATOR)
public final class FittingService {
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class Curves {
		public static final String GAUSSIAN = "gaussian";
		public static final String LORENTZIAN = "lorentzian";
		
		private static double gaussian(double x, double scale, double loc, double width) {
			return scale * Math.exp(-0.5 * Math.pow((x - loc) / width, 2.));
		}
		
		private static double lorentzian(double x, double scale, double loc, double width) {
			return scale / (Math.PI * (width + (Math.pow(x - loc, 2.) / width)));
		}
	}
	
	public void fit(Approximation approximation) {
		final MnUserParameters mnUserParameters = new MnUserParameters();
		
		for (final Curve curve : approximation.getCurves()) {
			mnUserParameters.add(UUID.randomUUID().toString(), curve.getMaxCrossSection().getValue(), 1.);
			mnUserParameters.add(UUID.randomUUID().toString(), curve.getEnergyAtMaxCrossSection().getValue(), 1.);
			mnUserParameters.add(UUID.randomUUID().toString(), curve.getFullWidthAtHalfMaximum().getValue(), 1.);
		}
		
		final ChiSquaredFCN fcn = new ChiSquaredFCN(approximation.getCurves(), approximation.getSourceData());
		final FunctionMinimum minimum = new MnMigrad(fcn, mnUserParameters).minimize();
		
		log.info("FunctionMinimum: " + minimum.toString());
		
		if (!minimum.isValid()) {
			throw new FittingException("Minimization did not converge");
		}
		
		approximation.setChiSquared(minimum.fval());
		approximation.setChiSquaredReduced(minimum.fval() / (approximation.getSourceData().size() - minimum.userParameters().variableParameters()));
		
		int paramIndex = 0;
		for (final Curve curve : approximation.getCurves()) {
			curve.getMaxCrossSection().setValue(minimum.userParameters().value(paramIndex));
			curve.getMaxCrossSection().setError(minimum.userParameters().error(paramIndex));
			
			paramIndex++;
			
			curve.getEnergyAtMaxCrossSection().setValue(minimum.userParameters().value(paramIndex));
			curve.getEnergyAtMaxCrossSection().setError(minimum.userParameters().error(paramIndex));
			
			paramIndex++;
			
			curve.getFullWidthAtHalfMaximum().setValue(minimum.userParameters().value(paramIndex));
			curve.getFullWidthAtHalfMaximum().setError(minimum.userParameters().error(paramIndex));
			
			paramIndex++;
		}
	}
	
	private static final class ChiSquaredFCN implements FCNBase {
		private final List<Curve> curves;
		private final List<DataPoint> sourceData;
		
		private ChiSquaredFCN(List<Curve> curves, List<DataPoint> sourceData) {
			this.curves = curves;
			this.sourceData = sourceData;
		}
		
		private double model(double x, double[] paramArray) {
			double sum = 0.;
			
			int paramIndex = 0;
			for (final Curve curve : curves) {
				switch (curve.getType()) {
					case Curves.GAUSSIAN: {
						final double maxCrossSection = paramArray[paramIndex++];
						final double energyAtMaxCrossSection = paramArray[paramIndex++];
						final double fullWidth = paramArray[paramIndex++];
						
						sum += Curves.gaussian(x, maxCrossSection, energyAtMaxCrossSection, fullWidth / (2. * Math.sqrt(2. * Math.log(2.))));
						
						break;
					}
					case Curves.LORENTZIAN: {
						final double maxCrossSection = paramArray[paramIndex++];
						final double energyAtMaxCrossSection = paramArray[paramIndex++];
						final double fullWidth = paramArray[paramIndex++];
						
						sum += Curves.lorentzian(x, (Math.PI / 2) * fullWidth * maxCrossSection, energyAtMaxCrossSection, fullWidth / 2.);
						
						break;
					}
					default: {
						throw new FittingException("Unsupported curve type: " + curve.getType());
					}
				}
			}
			
			return sum;
		}
		
		@Override
		public double valueOf(double[] paramArray) {
			double chiSquared = 0.;
			
			for (final DataPoint p : sourceData) {
				final double model = model(p.getEnergy().getValue(), paramArray);
				final double data = p.getCrossSection().getValue();
				final double dataError = p.getCrossSection().getError();
				
				chiSquared += Math.pow((data - model) / dataError, 2.);
			}
			
			return chiSquared;
		}
	}
}
