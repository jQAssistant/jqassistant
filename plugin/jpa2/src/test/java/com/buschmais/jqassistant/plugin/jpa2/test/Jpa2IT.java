package com.buschmais.jqassistant.plugin.jpa2.test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.core.analysis.test.AbstractAnalysisIT;
import com.buschmais.jqassistant.plugin.jpa2.impl.store.PersistenceDescriptor;
import com.buschmais.jqassistant.plugin.jpa2.impl.store.PersistenceUnitDescriptor;
import com.buschmais.jqassistant.plugin.jpa2.test.set.entity.JpaEntity;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.TypeDescriptorMatcher.typeDescriptor;
import static com.buschmais.jqassistant.plugin.jpa2.test.matcher.PersistenceUnitMatcher.persistenceUnitDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
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

    /**
     * Verifies scanning of persistence descriptors.
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void persistenceDescriptor() throws IOException, AnalyzerException {
        scanTestClassesDirectory();
        TestResult testResult = query("MATCH (p:PERSISTENCE) RETURN p");
        assertThat(testResult.getRows().size(), equalTo(1));
        List<? super PersistenceDescriptor> persistenceDescriptors = testResult.getColumn("p");
        PersistenceDescriptor persistenceDescriptor = (PersistenceDescriptor) persistenceDescriptors.get(0);
        assertThat(persistenceDescriptor.getVersion(),equalTo("2.0"));
        Set<PersistenceUnitDescriptor> persistenceUnits = persistenceDescriptor.getContains();
        assertThat(persistenceUnits, hasItem(persistenceUnitDescriptor("persistence-unit")));
        PersistenceUnitDescriptor persistenceUnit = persistenceUnits.iterator().next();
        assertThat(persistenceUnit.getDescription(), equalTo("description"));
        assertThat(persistenceUnit.getJtaDataSource(), equalTo("jtaDataSource"));
        assertThat(persistenceUnit.getNonJtaDataSource(), equalTo("nonJtaDataSource"));
        assertThat(persistenceUnit.getProvider(), equalTo("provider"));
        assertThat(persistenceUnit.getValidationMode(), equalTo("AUTO"));
        assertThat(persistenceUnit.getSharedCacheMode(), equalTo("ENABLE_SELECTIVE"));
        assertThat(persistenceUnit.getContains(), hasItem(typeDescriptor(JpaEntity.class)));
    }
}
