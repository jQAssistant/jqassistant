package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.api.model.JavaSuppressDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.suppress.Suppress;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

class SuppressIT extends AbstractJavaPluginIT {

    @Test
    void suppressAnnotationMustNotBeScanned() throws RuleException {
        scanClasses(Suppress.class);
        Result<Constraint> constraintResult = validateConstraint("suppress:SuppressAnnotationMustNotBeScanned");
        assertThat(constraintResult.getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(constraintResult.getRows().size(), equalTo(0));
        store.commitTransaction();
    }

    @Test
    void suppressedClass() throws RuleException {
        verifySuppress("suppress:Class", "suppress:SuppressedClass", "class");
    }

    @Test
    void suppressedField() throws RuleException {
        verifySuppress("suppress:Field", "suppress:SuppressedField", "field");
    }

    @Test
    void suppressedMethod() throws RuleException {
        verifySuppress("suppress:Method", "suppress:SuppressedMethod", "method");
    }

    @Test
    void suppressedMethodInPrimaryColumn() throws RuleException {
        verifySuppress("suppress:MethodInPrimaryColumn", "suppress:SuppressedMethodInPrimaryColumn", "method");
    }

    private void verifySuppress(String constraintId, String conceptId, String column) throws RuleException {
        scanClasses(Suppress.class);
        assertThat(validateConstraint(constraintId).getStatus(), equalTo(SUCCESS));
        Result<Concept> suppressClasses = applyConcept(conceptId);
        assertThat(suppressClasses.getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(suppressClasses.getRows().size(), equalTo(1));
        Map<String, Object> row = suppressClasses.getRows().get(0);
        JavaSuppressDescriptor suppressDescriptor = (JavaSuppressDescriptor) row.get(column);
        assertThat(asList(suppressDescriptor.getSuppressIds()), hasItem(constraintId));
        store.commitTransaction();
    }

}
