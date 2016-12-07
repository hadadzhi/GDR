package ru.cdfe.gdr.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoSuchColumnException extends RuntimeException {
    public NoSuchColumnException(int column, String subEntNumber) {
        super("Subent: " + subEntNumber + ", Column: " + column);
    }
}
