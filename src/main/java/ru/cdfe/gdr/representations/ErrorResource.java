package ru.cdfe.gdr.representations;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ErrorResource {
	private final String message;
	private final String errorId;
	private final Long timestamp;
	
	public ErrorResource(String message) {
		this.message = message;
		this.errorId = UUID.randomUUID().toString();
		this.timestamp = Instant.now().getEpochSecond();
	}
}
