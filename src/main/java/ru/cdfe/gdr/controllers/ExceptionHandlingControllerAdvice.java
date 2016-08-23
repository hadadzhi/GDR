package ru.cdfe.gdr.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.cdfe.gdr.exceptions.NoSuchRecordException;
import ru.cdfe.gdr.representations.ErrorResource;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.joining;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlingControllerAdvice {
	private static final String logTemplate = "Request: [%s], Exception: [%s]";
	
	private static String parseRequest(HttpServletRequest request) {
		final String method = request.getMethod();
		final String path = request.getRequestURI();
		final String query = request.getQueryString();
		
		final StringBuilder builder = new StringBuilder();
		
		builder.append(method).append(' ').append(path);
		
		if (query != null) {
			builder.append('?').append(query);
		}
		
		return builder.toString();
	}
	
	private static <T> T lastOfIterable(Iterable<T> iterable) {
		return StreamSupport.stream(iterable.spliterator(), false).reduce((r, e) -> e).orElse(null);
	}
	
	@ExceptionHandler({ DuplicateKeyException.class })
	@ResponseStatus(HttpStatus.CONFLICT)
	public void handleConflict(DuplicateKeyException e, HttpServletRequest request) {
		log.warn(String.format(logTemplate, parseRequest(request), e.toString()));
	}
	
	@ExceptionHandler({ NoSuchRecordException.class })
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public void handleNotFound(Throwable e, HttpServletRequest request) {
		log.trace(String.format(logTemplate, parseRequest(request), e.toString()));
	}
	
	@ExceptionHandler({ ConstraintViolationException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResource handleBadRequest(ConstraintViolationException e, HttpServletRequest request) {
		final String message = e.getConstraintViolations().stream()
			.map(v -> lastOfIterable(v.getPropertyPath()) + " " + v.getMessage())
			.collect(joining(", "));
		
		log.warn(String.format(logTemplate, parseRequest(request), e.toString()) + " " + message);
		return new ErrorResource(message);
	}
}
