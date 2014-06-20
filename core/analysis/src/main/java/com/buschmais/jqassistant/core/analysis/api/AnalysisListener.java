package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.Rule;

public interface AnalysisListener<E extends AnalysisListenerException> {

    void begin() throws E;

    void end() throws E;

    void beginConcept(Concept concept) throws E;

    void endConcept() throws E;

    void beginGroup(Group group) throws E;

    void endGroup() throws E;

    void beginConstraint(Constraint constraint) throws E;

    void endConstraint() throws E;

    void setResult(Result<? extends Rule> result) throws E;

}
