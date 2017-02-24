package ru.cdfe.gdr.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
    private double chiSquared;
    
    @Finite
    private double chiSquaredReduced;
    
    @Valid
    private List<DataPoint> sourceData;

    @NotEmpty
    @Valid
    private List<Curve> curves;
    
    public List<DataPoint> getSourceData() {
        if (sourceData == null) {
            sourceData = new ArrayList<>();
        }
        
        return sourceData;
    }
    
    public List<Curve> getCurves() {
        if (curves == null) {
            curves = new ArrayList<>();
        }
        
        return curves;
    }
}
