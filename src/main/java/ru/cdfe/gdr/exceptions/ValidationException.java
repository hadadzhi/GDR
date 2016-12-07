package ru.cdfe.gdr.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.joining;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {
    public <T> ValidationException(Set<ConstraintViolation<T>> violations) {
        super(violations.stream()
            .map(v -> StreamSupport.stream(v.getPropertyPath().spliterator(), false).reduce((r, e) -> e).orElse(null) + " " + v.getMessage())
            .collect(joining(", ")));
    }
}
