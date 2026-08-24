package com.buschmais.jqassistant.core.report.api.model;

import java.time.LocalDate;

import com.buschmais.jqassistant.core.store.api.model.jQAssistantDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("jQASuppression")
public interface SuppressionDescriptor extends jQAssistantDescriptor {

    String getRuleId();

    void setRuleId(String ruleId);

    String getColumn();

    void setColumn(String column);

    LocalDate getUntil();

    void setUntil(LocalDate until);

    String getReason();

    void setReason(String reason);

}
