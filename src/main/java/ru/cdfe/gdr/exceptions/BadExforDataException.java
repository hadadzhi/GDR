package ru.cdfe.gdr.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadExforDataException extends RuntimeException {
    public BadExforDataException(String message, String subEntNumber) {
        super("Subent: " + subEntNumber + ", Message: " + message);
    }
}
