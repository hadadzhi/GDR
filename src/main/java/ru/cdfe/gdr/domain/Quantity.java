package ru.cdfe.gdr.domain;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import ru.cdfe.gdr.validation.Finite;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Quantity {
    public static final String NO_DIM = "NO-DIM";
    
    @Finite
    private double value;
    
    @Finite
    private double error;
    
    @NotBlank
    private String dimension;
    
    public Quantity(double value) {
        this(value, 0., NO_DIM);
    }
    
    public Quantity(double value, String dimension) {
        this(value, 0., dimension);
    }
    
    public Quantity(double value, double error) {
        this(value, error, NO_DIM);
    }
    
    public Quantity(double value, double error, String dimension) {
        this.value = value;
        this.error = error;
        this.dimension = dimension;
    }
}
