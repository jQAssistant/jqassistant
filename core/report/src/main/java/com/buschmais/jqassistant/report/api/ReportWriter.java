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

    void begin() throws ReportWriterException;

    void end() throws ReportWriterException;

    void beginConcept(Concept concept) throws ReportWriterException;

    void endConcept() throws ReportWriterException;

    void beginConstraintGroup(ConstraintGroup constraintGroup) throws ReportWriterException;

    void endConstraintGroup() throws ReportWriterException;

    void beginConstraint(Constraint constraint) throws ReportWriterException;

    void endConstraint() throws ReportWriterException;

    void setResult(Result result) throws ReportWriterException;

}
