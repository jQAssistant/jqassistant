package com.buschmais.jqassistant.rules.javaee.test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.core.analysis.test.AbstractAnalysisIT;
import com.buschmais.jqassistant.rules.javaee.test.set.jpa.JpaEntity;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Tests for the JPA concepts.
 */
public class JpaIT extends AbstractAnalysisIT {

    /**
     * Verifies the concept "jpa:Entity".
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void jpaEntity() throws IOException, AnalyzerException {
        scanClasses(JpaEntity.class);
        applyConcept("jpa:Entity");
        assertThat(query("MATCH (e:TYPE:ENTITY) RETURN e").getColumn("e"), hasItem(typeDescriptor(JpaEntity.class)));
    }
}
