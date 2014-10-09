package com.buschmais.jqassistant.plugin.cdi.test.rule;

import static com.buschmais.jqassistant.core.analysis.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ResultMatcher.result;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.inject.BeanWithConstructorInjection;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.inject.BeanWithFieldInjection;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.inject.BeanWithSetterInjection;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Tests for CDI injection constraints.
 * 
 * @author Aparna Chaudhary
 */
public class CdiInjectionIT extends AbstractJavaPluginIT {

	/**
	 * Verifies the constraint "cdi:BeansMustUseConstructorInjection".
	 * 
	 * @throws java.io.IOException
	 *             If the test fails.
	 * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
	 *             If the test fails.
	 */
	@Test
	public void test_ConstructorInjection() throws IOException, AnalysisException {
		scanClasses(BeanWithFieldInjection.class);
		String ruleName = "cdi:BeansMustUseConstructorInjection";
		validateConstraint(ruleName);
		store.beginTransaction();

		List<Result<Constraint>> constraintViolations =
                (List<Result<Constraint>>) reportWriter.getConstraintViolations().values();
		assertThat("Unexpected number of violated constraints", constraintViolations.size(), equalTo(1));
		Result<Constraint> result = constraintViolations.get(0);
		assertThat("Expected constraint " + ruleName, result, result(constraint(ruleName)));
		List<Map<String, Object>> violatedBeans = result.getRows();
		assertThat("Unexpected number of violations", violatedBeans.size(), equalTo(1));
		assertEquals("Unexpected bean name", violatedBeans.get(0).get("invalidBean"), BeanWithFieldInjection.class.getName());

		store.commitTransaction();
	}

	/**
	 * Verifies the constraint "cdi:BeansMustUseConstructorInjection" results in no violations when applied to valid
	 * beans.
	 * 
	 * @throws java.io.IOException
	 *             If the test fails.
	 * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
	 *             If the test fails.
	 */
	@Test
	public void test_ConstructorInjection_No_Violation() throws IOException, AnalysisException {
		scanClasses(BeanWithConstructorInjection.class);
		String ruleName = "cdi:BeansMustUseConstructorInjection";
		validateConstraint(ruleName);
		store.beginTransaction();

		List<Result<Constraint>> constraintViolations =
                (List<Result<Constraint>>) reportWriter.getConstraintViolations().values();
		assertThat("Unexpected number of violated constraints", constraintViolations.size(), equalTo(1));
		Result<Constraint> result = constraintViolations.get(0);
		assertThat("Expected constraint " + ruleName, result, result(constraint(ruleName)));
		List<Map<String, Object>> violatedBeans = result.getRows();
		assertThat("Unexpected number of violations", violatedBeans.size(), equalTo(0));

		store.commitTransaction();
	}

	/**
	 * Verifies the constraint "cdi:BeansMustNotUseFieldInjection" results in no violations when applied to beans with
	 * setter or constructor injection.
	 * 
	 * @throws java.io.IOException
	 *             If the test fails.
	 * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
	 *             If the test fails.
	 */
	@Test
	public void test_FieldInjection_No_Violation() throws IOException, AnalysisException {
		scanClasses(BeanWithConstructorInjection.class);
		scanClasses(BeanWithSetterInjection.class);
		String ruleName = "cdi:BeansMustNotUseFieldInjection";
		validateConstraint(ruleName);
		store.beginTransaction();

		List<Result<Constraint>> constraintViolations =
                (List<Result<Constraint>>) reportWriter.getConstraintViolations().values();
		assertThat("Unexpected number of violated constraints", constraintViolations.size(), equalTo(1));
		Result<Constraint> result = constraintViolations.get(0);
		assertThat("Expected constraint " + ruleName, result, result(constraint(ruleName)));
		List<Map<String, Object>> violations = result.getRows();
		assertThat("Unexpected number of violations", violations.size(), equalTo(0));

		store.commitTransaction();
	}
	
	/**
	 * Verifies the constraint "cdi:BeansMustNotUseFieldInjection".
	 * 
	 * @throws java.io.IOException
	 *             If the test fails.
	 * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
	 *             If the test fails.
	 */
	@Test
	public void test_BeanInjection() throws IOException, AnalysisException {
		scanClasses(BeanWithFieldInjection.class);
		String ruleName = "cdi:BeansMustNotUseFieldInjection";
		validateConstraint(ruleName);
		store.beginTransaction();

		List<Result<Constraint>> constraintViolations =
                (List<Result<Constraint>>) reportWriter.getConstraintViolations().values();
		assertThat("Unexpected number of violated constraints", constraintViolations.size(), equalTo(1));
		Result<Constraint> result = constraintViolations.get(0);
		assertThat("Expected constraint " + ruleName, result, result(constraint(ruleName)));
		List<Map<String, Object>> violations = result.getRows();
		assertThat("Unexpected number of violations", violations.size(), equalTo(1));
		assertEquals("Unexpected bean name", violations.get(0).get("invalidBean"), BeanWithFieldInjection.class.getName());

		store.commitTransaction();
	}

}
