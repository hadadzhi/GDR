package ru.cdfe.gdr.services;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.freehep.math.minuit.FCNBase;
import org.freehep.math.minuit.FunctionMinimum;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnUserParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.cdfe.gdr.domain.Approximation;
import ru.cdfe.gdr.domain.Curve;
import ru.cdfe.gdr.domain.DataPoint;
import ru.cdfe.gdr.domain.Quantity;
import ru.cdfe.gdr.exceptions.FittingException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public final class FittingService {
	private static final String PREFIX_LOCATION = "energy";
	private static final String PREFIX_AMPLITUDE = "cs";
	private static final String PREFIX_FWHM = "fwhm";
	private static final String PARAM_NAME_FORMAT = "%s%d";
	
	private static String paramName(String prefix, int index) {
		return String.format(PARAM_NAME_FORMAT, prefix, index);
	}
	
	public void fit(Approximation approximation) {
		final ChiSquaredFCN fcn = new ChiSquaredFCN(approximation.getCurves(), approximation.getSourceData());
		final FunctionMinimum minimum = new MnMigrad(fcn, fcn.getMnUserParameters()).minimize();
		
		log.info("FunctionMinimum: " + minimum.toString());
		
		if (!minimum.isValid()) {
			throw new FittingException("Minimization did not converge");
		}
		
		approximation.setChiSquared(minimum.fval());
		approximation.setChiSquaredReduced(minimum.fval() / (approximation.getSourceData().size() - minimum.userParameters().variableParameters()));
		
		for (int i = 0; i < approximation.getCurves().size(); i++) {
			final Curve curve = approximation.getCurves().get(i);
			
			curve.getEnergyAtMaxCrossSection().setValue(minimum.userParameters().value(paramName(PREFIX_LOCATION, i)));
			curve.getEnergyAtMaxCrossSection().setError(minimum.userParameters().error(paramName(PREFIX_LOCATION, i)));
			
			curve.getMaxCrossSection().setValue(minimum.userParameters().value(paramName(PREFIX_AMPLITUDE, i)));
			curve.getMaxCrossSection().setError(minimum.userParameters().error(paramName(PREFIX_AMPLITUDE, i)));
			
			curve.getFullWidthAtHalfMaximum().setValue(minimum.userParameters().value(paramName(PREFIX_FWHM, i)));
			curve.getFullWidthAtHalfMaximum().setError(minimum.userParameters().error(paramName(PREFIX_FWHM, i)));
		}
	}
	
	private static final class ChiSquaredFCN implements FCNBase {
		private final List<Curve> curves;
		private final List<DataPoint> sourceData;
		
		private final Map<String, Integer> paramIndices;
		
		@Getter
		private final MnUserParameters mnUserParameters;
		
		private ChiSquaredFCN(List<Curve> curves, List<DataPoint> sourceData) {
			this.curves = curves;
			this.sourceData = sourceData;
			
			this.paramIndices = new HashMap<>();
			this.mnUserParameters = new MnUserParameters();
			
			int paramIndex = 0;
			for (int i = 0; i < curves.size(); i++) {
				final Curve curve = curves.get(i);
				
				final double maxCrossSection = curve.getMaxCrossSection().getValue();
				mnUserParameters.add(paramName(PREFIX_AMPLITUDE, i), maxCrossSection, maxCrossSection);
				paramIndices.put(paramName(PREFIX_AMPLITUDE, i), paramIndex++);
				
				final double energyAtMaxCrossSection = curve.getEnergyAtMaxCrossSection().getValue();
				mnUserParameters.add(paramName(PREFIX_LOCATION, i), energyAtMaxCrossSection, energyAtMaxCrossSection);
				paramIndices.put(paramName(PREFIX_LOCATION, i), paramIndex++);
				
				final double fwhm = curve.getFullWidthAtHalfMaximum().getValue();
				mnUserParameters.add(paramName(PREFIX_FWHM, i), fwhm, fwhm);
				paramIndices.put(paramName(PREFIX_FWHM, i), paramIndex++);
			}
			
			log.info(paramIndices.toString());
		}
		
		@Override
		public double valueOf(double[] paramArray) {
			double chiSquared = 0.;
			
			for (DataPoint p : sourceData) {
				final double model = model(p.getEnergy().getValue(), paramArray);
				final double observed = p.getCrossSection().getValue();
				final double observedError = p.getCrossSection().getError();
				
				chiSquared += Math.pow((observed - model) / observedError, 2.);
			}
			
			return chiSquared;
		}
		
		private double model(double x, double[] paramArray) {
			double sum = 0.;
			
			for (int i = 0; i < curves.size(); i++) {
				final Curve curve = curves.get(i);
				
				switch (curve.getType()) {
					case Curves.GAUSSIAN: {
						final double loc = paramArray[paramIndices.get(paramName(PREFIX_LOCATION, i))];
						final double width = paramArray[paramIndices.get(paramName(PREFIX_FWHM, i))] / (2. * Math.sqrt(2. * Math.log(2.)));
						final double scale = paramArray[paramIndices.get(paramName(PREFIX_AMPLITUDE, i))];
						
						sum += Curves.gaussian(x, scale, loc, width);
						
						break;
					}
					case Curves.LORENTZIAN: {
						final double loc = paramArray[paramIndices.get(paramName(PREFIX_LOCATION, i))];
						final double width = paramArray[paramIndices.get(paramName(PREFIX_FWHM, i))] / 2.;
						final double scale = Math.PI * width * paramArray[paramIndices.get(paramName(PREFIX_AMPLITUDE, i))];
						
						sum += Curves.lorentzian(x, scale, loc, width);
						
						break;
					}
					default: {
						throw new FittingException("Unsupported curve type: " + curve.getType());
					}
				}
			}
			
			return sum;
		}
		
	}
	
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class Curves {
		public static final String GAUSSIAN = "gaussian";
		public static final String LORENTZIAN = "lorentzian";
		
		static double gaussian(double x, double scale, double loc, double width) {
			return scale * Math.exp(-0.5 * Math.pow((x - loc) / width, 2.));
		}
		
		static double lorentzian(double x, double scale, double loc, double width) {
			return scale / (Math.PI * (width + (Math.pow(x - loc, 2.) / width)));
		}
	}
}

@Slf4j
@Component
class TestFitting implements CommandLineRunner {
	private final ExforService exforService;
	private final FittingService fittingService;
	
	@Autowired
	public TestFitting(ExforService exforService, FittingService fittingService) {
		this.exforService = exforService;
		this.fittingService = fittingService;
	}
	
	@Override
	public void run(String... args) throws Exception {
		final List<DataPoint> sourceData = exforService.getData("L0028002", 0, 1, 2);
		
		final Curve curve1 = Curve.builder()
			.type(FittingService.Curves.LORENTZIAN)
			.maxCrossSection(new Quantity(50.))
			.energyAtMaxCrossSection(new Quantity(25.))
			.fullWidthAtHalfMaximum(new Quantity(10.))
			.build();
		
		final Curve curve2 = Curve.builder()
			.type(FittingService.Curves.LORENTZIAN)
			.maxCrossSection(new Quantity(50.))
			.energyAtMaxCrossSection(new Quantity(25.))
			.fullWidthAtHalfMaximum(new Quantity(10.))
			.build();
		
		final Approximation approximation = Approximation.builder()
			.curves(Arrays.asList(curve1, curve2))
			.sourceData(sourceData)
			.description("Test")
			.build();

		final long start = System.currentTimeMillis();
		
		fittingService.fit(approximation);
		
		final long end = System.currentTimeMillis();
		
		log.info(approximation.toString());
		log.info("Time to fit: " + (end - start) + " ms");
	}
}
