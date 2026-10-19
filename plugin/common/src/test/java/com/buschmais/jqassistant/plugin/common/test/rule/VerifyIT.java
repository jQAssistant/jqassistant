package com.buschmais.jqassistant.plugin.common.test.rule;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;

public class VerifyIT extends AbstractPluginIT {

    @Test
    public void validConceptRowCount() throws Exception {
        assertThat(applyConcept("concept:ValidRowCount").getStatus()).isEqualTo(SUCCESS);
        assertThat(applyConcept("concept:ValidExplicitRowCount").getStatus()).isEqualTo(SUCCESS);
    }

    @Test
    public void invalidConceptRowCount() throws Exception {
        Result<Concept> conceptResult1 = applyConcept("concept:InvalidRowCount");
        assertThat(conceptResult1.getVerificationResult()
            .isSuccess()).isFalse();
        assertThat(conceptResult1.getStatus()).isEqualTo(SUCCESS);
        Result<Concept> conceptResult2 = applyConcept("concept:InvalidExplicitRowCount");
        assertThat(conceptResult2.getStatus()).isEqualTo(SUCCESS);
        assertThat(conceptResult2.getStatus()).isEqualTo(SUCCESS);
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
        Result<Concept> conceptResult1 = applyConcept("concept:InvalidAggregation");
        assertThat(conceptResult1.getStatus()).isEqualTo(SUCCESS);
        assertThat(conceptResult1.getVerificationResult()
            .isSuccess()).isFalse();
        Result<Concept> conceptResult2 = applyConcept("concept:InvalidAggregationWithColumn");
        assertThat(conceptResult2.getStatus()).isEqualTo(SUCCESS);
        assertThat(conceptResult2.getVerificationResult()
            .isSuccess()).isFalse();
    }

    @Test
    public void validConstraintAggregation() throws Exception {
        assertThat(validateConstraint("constraint:ValidAggregation").getStatus()).isEqualTo(SUCCESS);
        assertThat(validateConstraint("constraint:ValidAggregationWithColumn").getStatus()).isEqualTo(SUCCESS);
        assertThat(validateConstraint("constraint:InvalidAggregation").getStatus()).isEqualTo(FAILURE);
        assertThat(validateConstraint("constraint:InvalidAggregationWithColumn").getStatus()).isEqualTo(FAILURE);
    }

    @Test
    public void invalidConstraintAggregation() throws Exception {
        assertThat(validateConstraint("constraint:InvalidAggregation").getStatus()).isEqualTo(FAILURE);
        assertThat(validateConstraint("constraint:InvalidAggregationWithColumn").getStatus()).isEqualTo(FAILURE);
    }
}
