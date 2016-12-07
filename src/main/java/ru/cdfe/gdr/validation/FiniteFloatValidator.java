package ru.cdfe.gdr.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FiniteFloatValidator implements ConstraintValidator<Finite, Float> {
    @Override
    public void initialize(Finite constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(Float value, ConstraintValidatorContext context) {
        return value == null || !value.isInfinite() && !value.isNaN();
    }
}
