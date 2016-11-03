package ru.cdfe.gdr.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated element's value must not be infinite or NaN. {@code null} elements are considered valid. Accepts {@link Float} and {@link Double}.
 */
@Documented
@Target({ ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { FiniteDoubleValidator.class, FiniteFloatValidator.class })
public @interface Finite {
  String message() default "may not be infinite or NaN";
  
  Class<?>[] groups() default {};
  
  Class<? extends Payload>[] payload() default {};
}
