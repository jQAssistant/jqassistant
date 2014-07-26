package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.analysis.rules.schema.v1.SeverityEnum;

/**
 * Defines a constraint to be validated.
 */
public class Constraint extends AbstractRule {

	/**
	 * The severity of the constraint.
	 */
	private SeverityEnum severity;

	/**
	 * Returns the severity of the constraint.
	 * 
	 * @return severity value
	 */
	public SeverityEnum getSeverity() {
		return severity;
	}

	/**
	 * Returns the severity of the constraint.
	 * 
	 * @param severity
	 *            severity value
	 */
	public void setSeverity(SeverityEnum severity) {
		this.severity = severity;
	}

}
