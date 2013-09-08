package com.buschmais.jqassistant.plugin.jpa2.test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.core.analysis.test.AbstractAnalysisIT;
import com.buschmais.jqassistant.plugin.jpa2.impl.store.PersistenceDescriptor;
import com.buschmais.jqassistant.plugin.jpa2.impl.store.PersistenceUnitDescriptor;
import com.buschmais.jqassistant.plugin.jpa2.test.matcher.PersistenceUnitMatcher;
import com.buschmais.jqassistant.plugin.jpa2.test.set.entity.JpaEntity;
import org.hamcrest.core.IsCollectionContaining;
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
        TestResult testResult = query("MATCH (p:JPA:PERSISTENCE) RETURN p");
        assertThat(testResult.getRows().size(), equalTo(1));
        List<? super PersistenceDescriptor> persistenceDescriptors = testResult.getColumn("p");
        PersistenceDescriptor persistenceDescriptor = (PersistenceDescriptor) persistenceDescriptors.get(0);
        assertThat(persistenceDescriptor.getVersion(),equalTo("2.0"));
        Set<PersistenceUnitDescriptor> persistenceUnits = persistenceDescriptor.getContains();
        assertThat(persistenceUnits, hasItem(PersistenceUnitMatcher.persistenceUnitDescriptor("persistence-unit")));
    }

    /**
     * Verifies scanning of persistence unit descriptors.
     *
     * @throws java.io.IOException If the test fails.
     * @throws AnalyzerException   If the test fails.
     */
    @Test
    public void persistenceUnitDescriptor() throws IOException, AnalyzerException {
        scanTestClassesDirectory();
        TestResult testResult = query("MATCH (pu:JPA:PERSISTENCEUNIT) RETURN pu");
        assertThat(testResult.getRows().size(), equalTo(1));
        List<? super PersistenceUnitDescriptor> persistenceUnitDescriptors = testResult.getColumn("pu");
        PersistenceUnitDescriptor persistenceUnitDescriptor = (PersistenceUnitDescriptor) persistenceUnitDescriptors.get(0);
        assertThat(persistenceUnitDescriptor.getDescription(), equalTo("description"));
        assertThat(persistenceUnitDescriptor.getJtaDataSource(), equalTo("jtaDataSource"));
        assertThat(persistenceUnitDescriptor.getNonJtaDataSource(), equalTo("nonJtaDataSource"));
        assertThat(persistenceUnitDescriptor.getProvider(), equalTo("provider"));
        assertThat(persistenceUnitDescriptor.getValidationMode(), equalTo("AUTO"));
        assertThat(persistenceUnitDescriptor.getSharedCacheMode(), equalTo("ENABLE_SELECTIVE"));
        assertThat(persistenceUnitDescriptor.getContains(), hasItem(typeDescriptor(JpaEntity.class)));
    }
}
