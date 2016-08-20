package ru.cdfe.gdr.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlers {
	private static final String LOG_TEMPLATE = "Exception processing request [%s]: ";
	@ExceptionHandler(OptimisticLockingFailureException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public void optimisticLockingFailure(OptimisticLockingFailureException e) {
		log.warn(String.format(LOG_TEMPLATE, currentUri()), e);
	}
	
	private static String currentUri() {
		return ServletUriComponentsBuilder.fromCurrentRequest().build().toString();
	}
}
