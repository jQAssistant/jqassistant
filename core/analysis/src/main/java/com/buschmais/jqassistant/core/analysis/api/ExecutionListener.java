package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.analysis.api.rule.AbstractExecutable;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;

/**
 * Created with IntelliJ IDEA. User: Dirk Mahler Date: 28.07.13 Time: 12:47 To
 * change this template use File | Settings | File Templates.
 */
public interface ExecutionListener {

	void begin() throws ExecutionListenerException;

	void end() throws ExecutionListenerException;

	void beginConcept(Concept concept) throws ExecutionListenerException;

	void endConcept() throws ExecutionListenerException;

	void beginGroup(Group group) throws ExecutionListenerException;

	void endGroup() throws ExecutionListenerException;

	void beginConstraint(Constraint constraint) throws ExecutionListenerException;

	void endConstraint() throws ExecutionListenerException;

	void setResult(Result<? extends AbstractExecutable> result) throws ExecutionListenerException;

}
