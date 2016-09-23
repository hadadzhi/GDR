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
    
    /**
     * Alters the given {@link Approximation} to best fit its source data.
     * This is done by minimizing the chi squared function.
     * The parameters of the curves in the given approximation are used as initial guesses.
     * After a successful call to this method, the given approximation will contain the fitted parameters of the curves
     * as well as the minimized chi squared value.
     * If an exception was thrown by the call to this method, the given approximation remains as it was before the call.
     *
     * @param approximation an {@link Approximation} instance containing the source data and the curves to be fitted
     *                      with initial guesses as their parameters
     * @throws FittingException if the minimization of chi squared did not converge or if the given approximation object is invalid.
     */
    public void fit(Approximation approximation) {
        final MnUserParameters mnUserParameters = new MnUserParameters();
        
        // The order in which the parameters are added here is very important
        // as they will be passed to the chi squared function as an array
        // Use this loop for reference
        int parameterIndex = 0;
        for (final Curve curve : approximation.getCurves()) {
            mnUserParameters.add(Integer.toString(parameterIndex++), curve.getMaxCrossSection().getValue(), 1.);
            mnUserParameters.add(Integer.toString(parameterIndex++), curve.getEnergyAtMaxCrossSection().getValue(), 1.);
            mnUserParameters.add(Integer.toString(parameterIndex++), curve.getFullWidthAtHalfMaximum().getValue(), 1.);
        }
        
        final ChiSquaredFCN fcn = new ChiSquaredFCN(approximation.getCurves(), approximation.getSourceData());
        final FunctionMinimum min = new MnMigrad(fcn, mnUserParameters).minimize();
        
        log.info("FunctionMinimum: " + min.toString());
        
        if (!min.isValid()) {
            throw new FittingException("Minimization did not converge");
        }
        
        approximation.setChiSquared(min.fval());
        approximation.setChiSquaredReduced(min.fval() / (approximation.getSourceData().size() - min.userParameters().variableParameters()));
        
        parameterIndex = 0;
        for (final Curve curve : approximation.getCurves()) {
            curve.getMaxCrossSection().setValue(min.userParameters().value(parameterIndex));
            curve.getMaxCrossSection().setError(min.userParameters().error(parameterIndex));
            
            parameterIndex++;
            
            curve.getEnergyAtMaxCrossSection().setValue(min.userParameters().value(parameterIndex));
            curve.getEnergyAtMaxCrossSection().setError(min.userParameters().error(parameterIndex));
            
            parameterIndex++;
            
            curve.getFullWidthAtHalfMaximum().setValue(min.userParameters().value(parameterIndex));
            curve.getFullWidthAtHalfMaximum().setError(min.userParameters().error(parameterIndex));
            
            parameterIndex++;
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
                final double maxCrossSection = paramArray[paramIndex++];
                final double energyAtMaxCrossSection = paramArray[paramIndex++];
                final double fullWidth = paramArray[paramIndex++];
                
                switch (curve.getType()) {
                    case Curves.GAUSSIAN: {
                        sum += Curves.gaussian(x, maxCrossSection, energyAtMaxCrossSection, fullWidth / (2. * Math.sqrt(2. * Math.log(2.))));
                        break;
                    }
                    case Curves.LORENTZIAN: {
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
