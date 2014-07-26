package com.buschmais.jqassistant.core.report.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import com.buschmais.jqassistant.core.analysis.api.Console;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.AbstractRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.xo.spi.reflection.AnnotatedType;

/**
 * Provides utility functionality for creating reports.
 */
public final class ReportHelper {

	private Console console;

	/**
	 * Constructor.
	 * 
	 * @param console
	 *            The console to use for printing messages.
	 */
	public ReportHelper(Console console) {
		this.console = console;
	}

	/**
	 * Return the {@link com.buschmais.jqassistant.core.report.api.LanguageElement} associated with a
	 * {@link com.buschmais.jqassistant.core.store.api.descriptor.Descriptor}.
	 * 
	 * @param descriptor
	 *            The descriptor.
	 * @return The resolved {@link com.buschmais.jqassistant.core.report.api.LanguageElement}
	 * 
	 * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException
	 */
	public static LanguageElement getLanguageElement(Descriptor descriptor) throws AnalysisListenerException {
		for (Class<?> descriptorType : descriptor.getClass().getInterfaces()) {
			AnnotatedType annotatedType = new AnnotatedType(descriptorType);
			Annotation languageAnnotation = annotatedType.getByMetaAnnotation(Language.class);
			if (languageAnnotation != null) {
				return getAnnotationValue(languageAnnotation, "value", LanguageElement.class);
			}
		}
		return null;
	}

	public static final String LOG_LINE_PREFIX = "  \"";

	/**
	 * Logs the given {@link com.buschmais.jqassistant.core.analysis.api.rule.RuleSet} on level info.
	 * 
	 * @param ruleSet
	 *            The {@link com.buschmais.jqassistant.core.analysis.api.rule.RuleSet} .
	 */
	public void printRuleSet(RuleSet ruleSet) {
		console.info("Groups [" + ruleSet.getGroups().size() + "]");
		for (Group group : ruleSet.getGroups().values()) {
			console.info(LOG_LINE_PREFIX + group.getId() + "\"");
		}
		console.info("Constraints [" + ruleSet.getConstraints().size() + "]");
		for (Constraint constraint : ruleSet.getConstraints().values()) {
			console.info(LOG_LINE_PREFIX + constraint.getId() + "\" - " + constraint.getDescription());
		}
		console.info("Concepts [" + ruleSet.getConcepts().size() + "]");
		for (Concept concept : ruleSet.getConcepts().values()) {
			console.info(LOG_LINE_PREFIX + concept.getId() + "\" - " + concept.getDescription());
		}
		if (!ruleSet.getMissingConcepts().isEmpty()) {
			console.info("Missing concepts [" + ruleSet.getMissingConcepts().size() + "]");
			for (String missingConcept : ruleSet.getMissingConcepts()) {
				console.warn(LOG_LINE_PREFIX + missingConcept);
			}
		}
		if (!ruleSet.getMissingConstraints().isEmpty()) {
			console.info("Missing constraints [" + ruleSet.getMissingConstraints().size() + "]");
			for (String missingConstraint : ruleSet.getMissingConstraints()) {
				console.warn(LOG_LINE_PREFIX + missingConstraint);
			}
		}
		if (!ruleSet.getMissingGroups().isEmpty()) {
			console.info("Missing groups [" + ruleSet.getMissingGroups().size() + "]");
			for (String missingGroup : ruleSet.getMissingGroups()) {
				console.warn(LOG_LINE_PREFIX + missingGroup);
			}
		}
	}

	/**
	 * Verifies the concept results returned by the
	 * {@link com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter} .
	 * <p>
	 * A warning is logged for each concept which did not return a result (i.e. has not been applied).
	 * </p>
	 * 
	 * @param inMemoryReportWriter
	 *            The {@link com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter} .
	 */
	public void verifyConceptResults(InMemoryReportWriter inMemoryReportWriter) {
		List<Result<Concept>> conceptResults = inMemoryReportWriter.getConceptResults();
		for (Result<Concept> conceptResult : conceptResults) {
			if (conceptResult.getRows().isEmpty()) {
				console.warn("Concept '" + conceptResult.getRule().getId() + "' returned an empty result.");
			}
		}
	}

	/**
	 * Verifies the constraint violations returned by the {@link InMemoryReportWriter}.
	 * 
	 * @param inMemoryReportWriter
	 *            The {@link InMemoryReportWriter}.
	 */
	public int verifyConstraintViolations(InMemoryReportWriter inMemoryReportWriter) throws AnalysisListenerException {
		return verifyConstraintViolations(Severity.INFO, inMemoryReportWriter);
	}

	/**
	 * Verifies the constraint violations returned by the {@link InMemoryReportWriter}. Returns the count of constraints
	 * having severity higher than the provided severity level.
	 * 
	 * @param severity
	 *            severity level to use for verification
	 * @param inMemoryReportWriter
	 *            The {@link InMemoryReportWriter}.
	 */
	public int verifyConstraintViolations(Severity severity, InMemoryReportWriter inMemoryReportWriter) throws AnalysisListenerException {
		List<Result<Constraint>> constraintViolations = inMemoryReportWriter.getConstraintViolations();
		int violations = 0;
		for (Result<Constraint> constraintViolation : constraintViolations) {
			if (!constraintViolation.isEmpty()) {
				Constraint constraint = constraintViolation.getRule();
				// severity level check
				if (constraint.getSeverity().getLevel() <= severity.getLevel()) {
					console.error(constraint.getId() + ": " + constraint.getDescription());
					for (Map<String, Object> columns : constraintViolation.getRows()) {
						StringBuilder message = new StringBuilder();
						for (Map.Entry<String, Object> entry : columns.entrySet()) {
							if (message.length() > 0) {
								message.append(", ");
							}
							message.append(entry.getKey());
							message.append('=');
							String stringValue = getStringValue(entry.getValue());
							message.append(stringValue);
						}
						console.error("  " + message.toString());
					}
					violations++;
				}
			}
		}
		return violations;
	}

	/**
	 * Converts a value to its string representation.
	 * 
	 * @param value
	 *            The value.
	 * @return The string representation
	 * @throws AnalysisListenerException
	 */
	private String getStringValue(Object value) throws AnalysisListenerException {
		if (value != null) {
			if (value instanceof Descriptor) {
				Descriptor descriptor = (Descriptor) value;
				LanguageElement elementValue = ReportHelper.getLanguageElement(descriptor);
				if (elementValue != null) {
					SourceProvider sourceProvider = elementValue.getSourceProvider();
					return sourceProvider.getName(descriptor);
				}
			} else if (value instanceof Iterable) {
				StringBuffer sb = new StringBuffer();
				for (Object o : ((Iterable) value)) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					sb.append(getStringValue(o));
				}
				return "[" + sb.toString() + "]";
			} else if (value instanceof Map) {
				StringBuffer sb = new StringBuffer();
				for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					sb.append(entry.getKey());
					sb.append(":");
					sb.append(getStringValue(entry.getValue()));
				}
				return "{" + sb.toString() + "}";
			}
			return value.toString();
		}
		return null;
	}

	/**
	 * Return a value from an annotation.
	 * 
	 * @param annotation
	 *            The annotation.
	 * @param value
	 *            The value.
	 * @param expectedType
	 *            The expected type.
	 * @param <T>
	 *            The expected type.
	 * @return The value.
	 * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException
	 *             If the value cannot be determined from the annotation.
	 */
	private static <T> T getAnnotationValue(Annotation annotation, String value, Class<T> expectedType) throws AnalysisListenerException {
		Class<? extends Annotation> annotationType = annotation.annotationType();
		Method valueMethod;
		try {
			valueMethod = annotationType.getDeclaredMethod(value);
		} catch (NoSuchMethodException e) {
			throw new AnalysisListenerException("Cannot resolve required method '" + value + "()' for '" + annotationType + "'.");
		}
		Object elementValue;
		try {
			elementValue = valueMethod.invoke(annotation);
		} catch (ReflectiveOperationException e) {
			throw new AnalysisListenerException("Cannot invoke method value() for " + annotationType);
		}
		return elementValue != null ? expectedType.cast(elementValue) : null;
	}

}
