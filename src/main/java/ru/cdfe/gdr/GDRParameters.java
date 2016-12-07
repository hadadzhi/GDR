package ru.cdfe.gdr;

import lombok.Getter;
import ru.cdfe.gdr.domain.DataPoint;
import ru.cdfe.gdr.domain.Quantity;

import java.util.List;
import java.util.function.IntToDoubleFunction;

@Getter
public final class GDRParameters {
    private final Quantity integratedCrossSection;
    private final Quantity firstMoment;
    private final Quantity energyCenter;
    
    public GDRParameters(List<DataPoint> sourceData) {
        if (sourceData == null) {
            throw new NullPointerException();
        }
        
        if (sourceData.size() < 2) {
            throw new IllegalArgumentException();
        }
        
        this.integratedCrossSection = computeIntegratedCrossSection(sourceData);
        this.firstMoment = computeFirstMoment(sourceData);
        this.energyCenter = computeEnergyCenter(sourceData, integratedCrossSection);
    }
    
    private double energyDelta(int i, List<DataPoint> sourceData) {
        return 0.5 * (sourceData.get(i < sourceData.size() - 1 ? i + 1 : i).getEnergy().getValue() - sourceData.get(i > 0 ? i - 1 : i).getEnergy().getValue());
    }
    
    private Quantity computeSum(int size, IntToDoubleFunction valueAt, IntToDoubleFunction errorAt, String dimension) {
        double value = 0;
        double errorSquared = 0;
        
        for (int i = 0; i < size; i++) {
            value += valueAt.applyAsDouble(i);
            errorSquared += Math.pow(errorAt.applyAsDouble(i), 2);
        }
        
        return new Quantity(value, Math.sqrt(errorSquared), dimension);
    }
    
    private Quantity computeIntegratedCrossSection(List<DataPoint> sourceData) {
        return computeSum(
            sourceData.size(),
            i -> sourceData.get(i).getCrossSection().getValue() * energyDelta(i, sourceData),
            i -> energyDelta(i, sourceData) * sourceData.get(i).getCrossSection().getError(),
            sourceData.get(0).getEnergy().getDimension() + "*" + sourceData.get(0).getCrossSection().getDimension()
        );
    }
    
    private Quantity computeFirstMoment(List<DataPoint> sourceData) {
        return computeSum(
            sourceData.size(),
            i -> (sourceData.get(i).getCrossSection().getValue() / sourceData.get(i).getEnergy().getValue()) * energyDelta(i, sourceData),
            i -> (energyDelta(i, sourceData) / sourceData.get(i).getEnergy().getValue()) * sourceData.get(i).getCrossSection().getError(),
            sourceData.get(0).getCrossSection().getDimension()
        );
    }
    
    private Quantity computeEnergyCenter(List<DataPoint> sourceData, Quantity integratedCrossSection) {
        final Quantity actualFirstMoment = computeSum(
            sourceData.size(),
            i -> sourceData.get(i).getCrossSection().getValue() * sourceData.get(i).getEnergy().getValue() * energyDelta(i, sourceData),
            i -> energyDelta(i, sourceData) * sourceData.get(i).getEnergy().getValue() * sourceData.get(i).getCrossSection().getError(),
            null // This is irrelevant
        );
        
        double a = actualFirstMoment.getValue();
        double b = integratedCrossSection.getValue();
        double da = actualFirstMoment.getError();
        double db = integratedCrossSection.getError();
        
        return new Quantity(a / b, Math.sqrt(Math.pow(da / b, 2) + Math.pow(a * db / (b * b), 2)), sourceData.get(0).getEnergy().getDimension());
    }
}
