package com.buschmais.jqassistant.core.rule.impl.reader;

import java.util.List;

import com.networknt.schema.Error;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
class ValidationResult {

    private boolean sourceWasEmpty;

    private List<Error> validationMessages;

    public boolean hasErrors() {
        return !validationMessages.isEmpty();
    }
}
