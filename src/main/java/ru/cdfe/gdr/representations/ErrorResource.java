package ru.cdfe.gdr.representations;

import lombok.Getter;

import java.time.Instant;

@Getter
public final class ErrorResource {
  private final String message;
  private final Long timestamp;
  
  public ErrorResource(String message) {
    this.message = message;
    this.timestamp = Instant.now().getEpochSecond();
  }
}
