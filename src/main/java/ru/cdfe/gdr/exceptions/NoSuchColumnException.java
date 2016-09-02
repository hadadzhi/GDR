package ru.cdfe.gdr.exceptions;

public class NoSuchColumnException extends RuntimeException {
	public NoSuchColumnException(int column, String subEntNumber) {
		super("Subent: " + subEntNumber + ", Column: " + column);
	}
}
