package com.buschmais.jqassistant.plugin.jaxrs.test.rule;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.jaxrs.test.set.beans.MyRestResource;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ResultMatcher.result;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Tests for JaxRS resource method constraints.
 *
 * @author Aparna Chaudhary
 */
public class ResourceMethodRulesIT extends AbstractJavaPluginIT {

    /**
     * Verifies the constraint {@code jaxrs:GetMustBeIdempotent}.
     *
     * @throws java.io.IOException                                           If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException If the test fails.
     * @throws NoSuchMethodException                                         If the test fails.
     */
    @Test
    public void getMustBeIdempotent() throws Exception {
        scanClasses(MyRestResource.class);
        String ruleName = "jaxrs:GetMustBeIdempotent";
        assertThat(validateConstraint(ruleName).getStatus(), equalTo(FAILURE));
        store.beginTransaction();

        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportWriter.getConstraintResults().values());
        assertThat("Unexpected number of violated constraints", constraintViolations.size(), equalTo(1));
        Result<Constraint> result = constraintViolations.get(0);
        assertThat("Expected constraint " + ruleName, result, result(constraint(ruleName)));
        List<Map<String, Object>> violatedBeans = result.getRows();
        assertThat("Unexpected number of violations", violatedBeans.size(), equalTo(1));
        assertEquals("Unexpected resource name", MyRestResource.class.getName(), violatedBeans.get(0).get("invalidResource"));
        assertEquals("Unexpected resource method name", "voidGetMethod", violatedBeans.get(0).get("invalidMethod"));

        store.commitTransaction();
    }

}
