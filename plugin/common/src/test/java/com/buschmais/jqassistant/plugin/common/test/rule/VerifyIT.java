package com.buschmais.jqassistant.plugin.common.test.rule;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class VerifyIT extends AbstractPluginIT {

    @Test
    public void validConceptRowCount() throws AnalysisException {
        assertThat(applyConcept("concept:ValidRowCount").getStatus(), equalTo(SUCCESS));
        assertThat(applyConcept("concept:ValidExplicitRowCount").getStatus(), equalTo(SUCCESS));
    }

    @Test
    public void invalidConceptRowCount() throws AnalysisException {
        assertThat(applyConcept("concept:InvalidRowCount").getStatus(), equalTo(FAILURE));
        assertThat(applyConcept("concept:InvalidExplicitRowCount").getStatus(), equalTo(FAILURE));
    }

    @Test
    public void validConstraintRowCount() throws AnalysisException {
        assertThat(validateConstraint("constraint:ValidRowCount").getStatus(), equalTo(SUCCESS));
        assertThat(validateConstraint("constraint:ValidExplicitRowCount").getStatus(), equalTo(SUCCESS));
    }

    @Test
    public void invalidConstraintRowCount() throws AnalysisException {
        assertThat(validateConstraint("constraint:InvalidRowCount").getStatus(), equalTo(FAILURE));
        assertThat(validateConstraint("constraint:InvalidExplicitRowCount").getStatus(), equalTo(FAILURE));
    }

    @Test
    public void validConceptAggregation() throws AnalysisException {
        assertThat(applyConcept("concept:ValidAggregation").getStatus(), equalTo(SUCCESS));
        assertThat(applyConcept("concept:ValidAggregationWithColumn").getStatus(), equalTo(SUCCESS));
    }

    @Test
    public void invalidConceptAggregation() throws AnalysisException {
        assertThat(applyConcept("concept:InvalidAggregation").getStatus(), equalTo(FAILURE));
        assertThat(applyConcept("concept:InvalidAggregationWithColumn").getStatus(), equalTo(FAILURE));
    }

    @Test
    public void vaildConstraintAggregation() throws AnalysisException {
        assertThat(validateConstraint("constraint:ValidAggregation").getStatus(), equalTo(SUCCESS));
        assertThat(validateConstraint("constraint:ValidAggregationWithColumn").getStatus(), equalTo(SUCCESS));
        assertThat(validateConstraint("constraint:InvalidAggregation").getStatus(), equalTo(FAILURE));
        assertThat(validateConstraint("constraint:InvalidAggregationWithColumn").getStatus(), equalTo(FAILURE));
    }

    @Test
    public void invaildConstraintAggregation() throws AnalysisException {
        assertThat(validateConstraint("constraint:InvalidAggregation").getStatus(), equalTo(FAILURE));
        assertThat(validateConstraint("constraint:InvalidAggregationWithColumn").getStatus(), equalTo(FAILURE));
    }
}
