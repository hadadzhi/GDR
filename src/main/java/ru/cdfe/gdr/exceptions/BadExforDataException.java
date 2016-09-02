package ru.cdfe.gdr.exceptions;

public class BadExforDataException extends RuntimeException {
	public BadExforDataException(String message, String subEntNumber) {
		super("Subent: " + subEntNumber + ", Message: " + message);
	}
}
