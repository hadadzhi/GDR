package ru.cdfe.gdr.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class DataPoint {
    @NotNull
    @Valid
    private Quantity energy;
    
    @NotNull
    @Valid
    private Quantity crossSection;
    
    public DataPoint(Quantity energy, Quantity crossSection) {
        this.energy = energy;
        this.crossSection = crossSection;
    }
}
