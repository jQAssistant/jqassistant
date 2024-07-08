package com.buschmais.jqassistant.core.scanner.impl;

/**
 * A specific {@link RuntimeException} that is thrown by the scanner if an
 * unrecoverable problem occurred.
 */
public class UnrecoverableScannerException extends RuntimeException {

    public UnrecoverableScannerException(String message, Throwable cause) {
        super(message, cause);
    }
}
