package ru.cdfe.gdr.exceptions;

public class NoExforDataException extends RuntimeException {
	public NoExforDataException(String subEntNumber) {
		super("Subent: " + subEntNumber);
	}
}
