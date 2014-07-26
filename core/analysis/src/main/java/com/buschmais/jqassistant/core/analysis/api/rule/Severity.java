package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.EnumSet;

/**
 * @author Aparna Chaudhary
 */
public enum Severity {

	/** */
	BLOCKER("blocker", 0),
	/** */
	CRITICAL("critical", 1),
	/** */
	MAJOR("major", 2),
	/** */
	MINOR("minor", 3),
	/** */
	INFO("info", 4);

	private final String value;
	private final Integer level;

	Severity(String value, Integer level) {
		this.value = value;
		this.level = level;
	}

	public String getValue() {
		return value;
	}

	public Integer getLevel() {
		return level;
	}

	public static Severity fromValue(String value) {
		for (Severity severity : Severity.values()) {
			if (severity.value.equals(value)) {
				return severity;
			}
		}
		throw new IllegalArgumentException(value);
	}

	public static Severity getSeverity(Integer level) {
		if (level == null) {
			return null;
		}
		EnumSet<Severity> enumSet = EnumSet.allOf(Severity.class);

		for (Severity severity : enumSet) {
			if (severity.level.equals(level)) {
				return severity;
			}
		}
		return null;
	}
	
}
