package ru.cdfe.gdr.domain;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Curve {
    @NotBlank
    private String type;
    
    @NotNull
    @Valid
    private Quantity maxCrossSection;
    
    @NotNull
    @Valid
    private Quantity energyAtMaxCrossSection;
    
    @NotNull
    @Valid
    private Quantity fullWidthAtHalfMaximum;
}
