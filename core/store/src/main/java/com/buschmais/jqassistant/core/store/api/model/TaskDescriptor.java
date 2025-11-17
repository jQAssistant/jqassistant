package com.buschmais.jqassistant.core.store.api.model;

import java.time.ZonedDateTime;

import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Represents a task executed by JQAssistant.
 */
@Abstract
@Label("Task")
public interface TaskDescriptor extends jQAssistantDescriptor {

    ZonedDateTime getTimestamp();

    void setTimestamp(ZonedDateTime timestamp);

}
