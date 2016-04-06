package com.buschmais.jqassistant.core.scanner.api;

/**
 * Represents the configuration of the scanner.
 */
public class ScannerConfiguration {

    private boolean continueOnError = false;

    public boolean isContinueOnError() {
        return continueOnError;
    }

    public void setContinueOnError(boolean continueOnError) {
        this.continueOnError = continueOnError;
    }
}
