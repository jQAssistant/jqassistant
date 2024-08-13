package com.buschmais.jqassistant.core.shared.xml;

import java.util.Optional;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static lombok.AccessLevel.PACKAGE;

@Slf4j
@RequiredArgsConstructor(access = PACKAGE)
public class JAXBValidationEventHandler implements ValidationEventHandler {

    private final Optional<String> sourceId;

    @Override
    public boolean handleEvent(ValidationEvent event) {
        ValidationEventLocator locator = event.getLocator();
        log.warn("XML problem while reading '{}' ({}:{}): {}", sourceId.orElse("unknown source."), locator.getLineNumber(), locator.getColumnNumber(),
                event.getMessage());
        return true;
    }
}
