package com.ghosthunter.exception;

/**
 * Exception thrown when heat map data is invalid.
 */
public class InvalidHeatMapDataException extends RuntimeException {

    public InvalidHeatMapDataException(String message) {
        super(message);
    }

    public InvalidHeatMapDataException(String message, Throwable cause) {
        super(message, cause);
    }
}