package com.buschmais.jqassistant.plugin.jpa2.test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.core.analysis.test.AbstractAnalysisIT;
import com.buschmais.jqassistant.plugin.jpa2.test.set.entity.JpaEntity;
import org.junit.Test;

import java.io.IOException;

import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Tests for the JPA concepts.
 */
public class Jpa2IT extends AbstractAnalysisIT {

    /**
     * Verifies the concept "entity:Entity".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void jpaEntity() throws IOException, AnalyzerException {
        scanClasses(JpaEntity.class);
        applyConcept("jpa2:Entity");
        assertThat(query("MATCH (e:TYPE:ENTITY) RETURN e").getColumn("e"), hasItem(typeDescriptor(JpaEntity.class)));
    }
}
