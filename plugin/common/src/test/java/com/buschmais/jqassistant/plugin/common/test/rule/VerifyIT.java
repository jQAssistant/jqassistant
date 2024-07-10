package com.buschmais.jqassistant.plugin.common.test.rule;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

public class VerifyIT extends com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT {

    @Test
    public void validConceptRowCount() throws Exception {
        assertThat(applyConcept("concept:ValidRowCount").getStatus()).isEqualTo(SUCCESS);
        assertThat(applyConcept("concept:ValidExplicitRowCount").getStatus()).isEqualTo(SUCCESS);
    }

    @Test
    public void invalidConceptRowCount() throws Exception {
        assertThat(applyConcept("concept:InvalidRowCount").getStatus()).isEqualTo(WARNING);
        assertThat(applyConcept("concept:InvalidExplicitRowCount").getStatus()).isEqualTo(WARNING);
    }

    @Test
    public void validConstraintRowCount() throws Exception {
        assertThat(validateConstraint("constraint:ValidRowCount").getStatus()).isEqualTo(SUCCESS);
        assertThat(validateConstraint("constraint:ValidExplicitRowCount").getStatus()).isEqualTo(SUCCESS);
    }

    @Test
    public void invalidConstraintRowCount() throws Exception {
        assertThat(validateConstraint("constraint:InvalidRowCount").getStatus()).isEqualTo(FAILURE);
        assertThat(validateConstraint("constraint:InvalidExplicitRowCount").getStatus()).isEqualTo(FAILURE);
    }

    @Test
    public void validConceptAggregation() throws Exception {
        assertThat(applyConcept("concept:ValidAggregation").getStatus()).isEqualTo(SUCCESS);
        assertThat(applyConcept("concept:ValidAggregationWithColumn").getStatus()).isEqualTo(SUCCESS);
    }

    @Test
    public void invalidConceptAggregation() throws Exception {
        assertThat(applyConcept("concept:InvalidAggregation").getStatus()).isEqualTo(WARNING);
        assertThat(applyConcept("concept:InvalidAggregationWithColumn").getStatus()).isEqualTo(WARNING);
    }

    @Test
    public void vaildConstraintAggregation() throws Exception {
        assertThat(validateConstraint("constraint:ValidAggregation").getStatus()).isEqualTo(SUCCESS);
        assertThat(validateConstraint("constraint:ValidAggregationWithColumn").getStatus()).isEqualTo(SUCCESS);
        assertThat(validateConstraint("constraint:InvalidAggregation").getStatus()).isEqualTo(FAILURE);
        assertThat(validateConstraint("constraint:InvalidAggregationWithColumn").getStatus()).isEqualTo(FAILURE);
    }

    @Test
    public void invaildConstraintAggregation() throws Exception {
        assertThat(validateConstraint("constraint:InvalidAggregation").getStatus()).isEqualTo(FAILURE);
        assertThat(validateConstraint("constraint:InvalidAggregationWithColumn").getStatus()).isEqualTo(FAILURE);
    }
}
