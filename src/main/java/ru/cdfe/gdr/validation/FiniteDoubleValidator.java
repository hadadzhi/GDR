package ru.cdfe.gdr.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FiniteDoubleValidator implements ConstraintValidator<Finite, Double> {
    @Override
    public void initialize(Finite constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        return value == null || !value.isInfinite() && !value.isNaN();
    }
}
