package com.buschmais.jqassistant.report.api;

import com.buschmais.jqassistant.core.model.api.Concept;
import com.buschmais.jqassistant.core.model.api.Constraint;
import com.buschmais.jqassistant.core.model.api.ConstraintGroup;
import com.buschmais.jqassistant.core.model.api.Result;

/**
 * Created with IntelliJ IDEA.
 * User: Dirk Mahler
 * Date: 28.07.13
 * Time: 12:47
 * To change this template use File | Settings | File Templates.
 */
public interface ReportWriter {

    void begin();

    void end();

    void beginConcept(Concept concept);

    void endConcept();

    void beginConstraintGroup(ConstraintGroup constraintGroup);

    void endConstraintGroup();

    void beginConstraint(Constraint constraint);

    void endConstraint();

    void setResult(Result result);

}
