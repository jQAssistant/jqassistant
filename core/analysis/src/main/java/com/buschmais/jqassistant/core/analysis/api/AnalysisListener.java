package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.Rule;

/**
 * Created with IntelliJ IDEA. User: Dirk Mahler Date: 28.07.13 Time: 12:47 To
 * change this template use File | Settings | File Templates.
 */
public interface AnalysisListener {

    void begin() throws AnalysisListenerException;

    void end() throws AnalysisListenerException;

    void beginConcept(Concept concept) throws AnalysisListenerException;

    void endConcept() throws AnalysisListenerException;

    void beginGroup(Group group) throws AnalysisListenerException;

    void endGroup() throws AnalysisListenerException;

    void beginConstraint(Constraint constraint) throws AnalysisListenerException;

    void endConstraint() throws AnalysisListenerException;

    void setResult(Result<? extends Rule> result) throws AnalysisListenerException;

}
