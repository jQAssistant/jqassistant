package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.EnumSet;

/**
 * Represents level of rule violations.
 * 
 * @author Aparna Chaudhary
 */
public enum Severity {

	BLOCKER("blocker", 0),
	CRITICAL("critical", 1),
	MAJOR("major", 2),
	MINOR("minor", 3),
	INFO("info", 4);

	private final String value;
	private final Integer level;

	/**
	 * Constructor
	 * 
	 * @param value
	 *            value of severity
	 * @param level
	 *            violation level
	 */
	Severity(String value, Integer level) {
		this.value = value;
		this.level = level;
	}

	/**
	 * Returns string representation of severity
	 * 
	 * @return string representation
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns violation level
	 * 
	 * @return violation level
	 */
	public Integer getLevel() {
		return level;
	}

	/**
	 * Retrieves severity based on string representation.
	 * 
	 * @param value
	 *            string representation; {@code null} if no matching severity found.
	 * @return {@link Severity}
	 */
	public static Severity fromValue(String value) {
		if (value == null) {
			return null;
		}
		for (Severity severity : EnumSet.allOf(Severity.class)) {
			if (severity.value.equals(value)) {
				return severity;
			}
		}
		return null;
	}

	/**
	 * Retrieves severity based on violation level.
	 * 
	 * @param level
	 *            violation level; {@code null} if no matching severity found.
	 * @return {@link Severity}
	 */
	public static Severity fromLevel(Integer level) {
		if (level == null) {
			return null;
		}
		for (Severity severity : EnumSet.allOf(Severity.class)) {
			if (severity.level.equals(level)) {
				return severity;
			}
		}
		return null;
	}

}
