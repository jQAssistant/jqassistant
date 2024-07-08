package com.buschmais.jqassistant.core.rule.impl.reader;

import java.util.Set;

import com.networknt.schema.ValidationMessage;

class ValidationResult {
    private boolean sourceWasEmpty;
    private Set<ValidationMessage> validationMessages;

    public boolean isSourceWasEmpty() {
        return sourceWasEmpty;
    }

    public void setSourceWasEmpty(boolean sourceWasEmpty) {
        this.sourceWasEmpty = sourceWasEmpty;
    }

    public Set<ValidationMessage> getValidationMessages() {
        return validationMessages;
    }

    public void setValidationMessages(Set<ValidationMessage> validationMessages) {
        this.validationMessages = validationMessages;
    }

    public boolean hasErrors() {
        return !validationMessages.isEmpty();
    }
}
