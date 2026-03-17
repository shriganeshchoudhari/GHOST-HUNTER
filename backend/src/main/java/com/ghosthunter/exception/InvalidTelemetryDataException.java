package com.ghosthunter.exception;

/**
 * Exception thrown when invalid telemetry data is provided.
 */
public class InvalidTelemetryDataException extends RuntimeException {

    public InvalidTelemetryDataException(String message) {
        super(message);
    }

    public InvalidTelemetryDataException(String message, Throwable cause) {
        super(message, cause);
    }
}