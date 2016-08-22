package ru.cdfe.gdr.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.cdfe.gdr.exceptions.InvalidRecordException;
import ru.cdfe.gdr.exceptions.RecordNotFoundException;
import ru.cdfe.gdr.representations.ErrorResource;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerControllerAdvice {
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
	
	@ExceptionHandler
	public ResponseEntity<?> handleDuplicateKey(DuplicateKeyException e, HttpServletRequest request) {
		log.warn(String.format(logTemplate, parseRequest(request), e.toString()));
		return ResponseEntity.status(HttpStatus.CONFLICT).build();
	}
	
	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public void handleNotFound(RecordNotFoundException e, HttpServletRequest request) {
		log.trace(String.format(logTemplate, parseRequest(request), e.toString()));
	}
	
	@ExceptionHandler
	public ResponseEntity<ErrorResource> handleBadRequest(InvalidRecordException e, HttpServletRequest request) {
		log.trace(String.format(logTemplate, parseRequest(request), e.toString()));
		return ResponseEntity.badRequest().body(new ErrorResource(e.getMessage()));
	}
}
