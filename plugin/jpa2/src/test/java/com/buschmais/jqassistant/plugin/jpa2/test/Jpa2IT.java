package com.buschmais.jqassistant.plugin.jpa2.test;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ResultMatcher.result;
import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.ValueDescriptorMatcher.valueDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.plugin.common.api.model.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.classpath.resolve.a.ClassType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.classpath.resolve.b.DependentType;
import com.buschmais.jqassistant.plugin.jpa2.api.model.PersistenceUnitDescriptor;
import com.buschmais.jqassistant.plugin.jpa2.api.model.PersistenceXmlDescriptor;
import com.buschmais.jqassistant.plugin.jpa2.test.matcher.PersistenceUnitMatcher;
import com.buschmais.jqassistant.plugin.jpa2.test.set.entity.JpaEmbeddable;
import com.buschmais.jqassistant.plugin.jpa2.test.set.entity.JpaEntity;

/**
 * Tests for the JPA concepts.
 */
public class Jpa2IT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "jpa2:Entity".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void entity() throws Exception {
        scanClasses(JpaEntity.class);
        assertThat(applyConcept("jpa2:Entity").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (e:Type:Jpa:Entity) RETURN e").getColumn("e"), hasItem(typeDescriptor(JpaEntity.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "jpa2:Embeddable".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void embeddable() throws Exception {
        scanClasses(JpaEmbeddable.class);
        assertThat(applyConcept("jpa2:Embeddable").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (e:Type:Jpa:Embeddable) RETURN e").getColumn("e"), hasItem(typeDescriptor(JpaEmbeddable.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "jpa2:Embedded".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void embedded() throws Exception {
        scanClasses(JpaEntity.class);
        assertThat(applyConcept("jpa2:Embedded").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> members = query("MATCH (e:Jpa:Embedded) RETURN e").getColumn("e");
        assertThat(members, hasItem(fieldDescriptor(JpaEntity.class, "embedded")));
        assertThat(members, hasItem(methodDescriptor(JpaEntity.class, "getEmbedded")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "jpa2:EmbeddedId".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void embeddedId() throws Exception {
        scanClasses(JpaEntity.class);
        assertThat(applyConcept("jpa2:EmbeddedId").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> members = query("MATCH (e:Jpa:EmbeddedId) RETURN e").getColumn("e");
        assertThat(members, hasItem(fieldDescriptor(JpaEntity.class, "id")));
        assertThat(members, hasItem(methodDescriptor(JpaEntity.class, "getId")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "jpa2:NamedQuery".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void namedQuery() throws Exception {
        scanClasses(JpaEntity.class);
        assertThat(applyConcept("jpa2:NamedQuery").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        TestResult query = query("MATCH (e:Entity)-[:DEFINES]->(n:Jpa:NamedQuery) RETURN n.name as name, n.query as query");
        List<Map<String, Object>> rows = query.getRows();
        assertThat(rows.size(), equalTo(1));
        Map<String, Object> row = rows.get(0);
        assertThat((String) row.get("name"), equalTo(JpaEntity.TESTQUERY_NAME));
        assertThat((String) row.get("query"), equalTo(JpaEntity.TESTQUERY_QUERY));
        store.commitTransaction();
    }
    
    /**
     * Verifies the uniqueness of concept "jpa2:NamedQuery" with keeping existing properties.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void namedQueryUniqueDifferentQuery() throws Exception {
        scanClasses(JpaEntity.class);
        assertThat(applyConcept("jpa2:Entity").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (e:Jpa:Entity {name: 'JpaEntity'}) CREATE (e)-[:DEFINES]->(n:Jpa:NamedQuery {name: 'testQuery', prop: 'value', query: 'foo'}) RETURN n").getColumn("n").size(), equalTo(1));
        assertThat(query("CREATE (n:Jpa:NamedQuery {name: 'otherQuery', query: 'SELECT e'}) RETURN n").getColumn("n").size(), equalTo(1));
        verifyUniqueRelation(1, 0, 0);
        store.commitTransaction();
        assertThat(applyConcept("jpa2:NamedQuery").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation(1, 1, 0);
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of concept "jpa2:NamedQuery" with keeping existing properties.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void namedQueryUniqueSameQuery() throws Exception {
        scanClasses(JpaEntity.class);
        assertThat(applyConcept("jpa2:Entity").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (e:Jpa:Entity {name: 'JpaEntity'}) CREATE (e)-[:DEFINES]->(n:Jpa:NamedQuery {name: 'testQuery', prop: 'value', query: 'SELECT e FROM JpaEntity e'}) RETURN n").getColumn("n").size(), equalTo(1));
        assertThat(query("CREATE (n:Jpa:NamedQuery {name: 'otherQuery', query: 'SELECT e'}) RETURN n").getColumn("n").size(), equalTo(1));
        verifyUniqueRelation(1, 1, 0);
        store.commitTransaction();
        assertThat(applyConcept("jpa2:NamedQuery").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation(1, 1, 0);
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of concept "jpa2:NamedQuery" with keeping existing properties.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void namedQueryUniqueWithoutQuery() throws Exception {
        scanClasses(JpaEntity.class);
        assertThat(applyConcept("jpa2:Entity").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (e:Jpa:Entity {name: 'JpaEntity'}) CREATE (e)-[:DEFINES]->(n:Jpa:NamedQuery {name: 'testQuery', prop: 'value'}) RETURN n").getColumn("n").size(), equalTo(1));
        assertThat(query("CREATE (n:Jpa:NamedQuery {name: 'otherQuery', query: 'SELECT e'}) RETURN n").getColumn("n").size(), equalTo(1));
        verifyUniqueRelation(1, 0, 1);
        store.commitTransaction();
        assertThat(applyConcept("jpa2:NamedQuery").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation(1, 1, 0);
        store.commitTransaction();
    }

    /**
     * Verifies scanning of persistence descriptors.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void fullPersistenceDescriptor() throws IOException, AnalysisException {
        scanClassPathDirectory(new File(getClassesDirectory(JpaEntity.class), "full"));
        store.beginTransaction();
        TestResult testResult = query("MATCH (p:Jpa:Persistence:Xml) RETURN p");
        assertThat(testResult.getRows().size(), equalTo(1));
        List<? super PersistenceXmlDescriptor> persistenceDescriptors = testResult.getColumn("p");
        PersistenceXmlDescriptor persistenceXmlDescriptor = (PersistenceXmlDescriptor) persistenceDescriptors.get(0);
        assertThat(persistenceXmlDescriptor.getVersion(), equalTo("2.0"));
        List<PersistenceUnitDescriptor> persistenceUnits = persistenceXmlDescriptor.getContains();
        assertThat(persistenceUnits, hasItem(PersistenceUnitMatcher.persistenceUnitDescriptor("persistence-unit")));
        store.commitTransaction();
    }

    /**
     * Verifies scanning of persistence unit descriptors.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void fullPersistenceUnitDescriptor() throws IOException, AnalysisException {
        scanClassPathDirectory(new File(getClassesDirectory(JpaEntity.class), "full"));
        store.beginTransaction();
        TestResult testResult = query("MATCH (pu:Jpa:PersistenceUnit) RETURN pu");
        assertThat(testResult.getRows().size(), equalTo(1));
        List<? super PersistenceUnitDescriptor> persistenceUnitDescriptors = testResult.getColumn("pu");
        PersistenceUnitDescriptor persistenceUnitDescriptor = (PersistenceUnitDescriptor) persistenceUnitDescriptors.get(0);
        assertThat(persistenceUnitDescriptor.getName(), equalTo("persistence-unit"));
        assertThat(persistenceUnitDescriptor.getTransactionType(), equalTo("RESOURCE_LOCAL"));
        assertThat(persistenceUnitDescriptor.getDescription(), equalTo("description"));
        assertThat(persistenceUnitDescriptor.getJtaDataSource(), equalTo("jtaDataSource"));
        assertThat(persistenceUnitDescriptor.getNonJtaDataSource(), equalTo("nonJtaDataSource"));
        assertThat(persistenceUnitDescriptor.getProvider(), equalTo("provider"));
        assertThat(persistenceUnitDescriptor.getValidationMode(), equalTo("AUTO"));
        assertThat(persistenceUnitDescriptor.getSharedCacheMode(), equalTo("ENABLE_SELECTIVE"));
        assertThat(persistenceUnitDescriptor.getContains(), hasItem(typeDescriptor(JpaEntity.class)));
        Matcher<? super PropertyDescriptor> valueMatcher = valueDescriptor("stringProperty", equalTo("stringValue"));
        assertThat(persistenceUnitDescriptor.getProperties(), hasItem(valueMatcher));
        store.commitTransaction();
    }

    /**
     * Verifies scanning of persistence descriptors.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void minimalPersistenceDescriptor() throws IOException, AnalysisException {
        scanClassPathDirectory(new File(getClassesDirectory(JpaEntity.class), "minimal"));
        store.beginTransaction();
        TestResult testResult = query("MATCH (p:Jpa:Persistence:Xml) RETURN p");
        assertThat(testResult.getRows().size(), equalTo(1));
        List<? super PersistenceXmlDescriptor> persistenceDescriptors = testResult.getColumn("p");
        PersistenceXmlDescriptor persistenceXmlDescriptor = (PersistenceXmlDescriptor) persistenceDescriptors.get(0);
        assertThat(persistenceXmlDescriptor.getVersion(), equalTo("2.0"));
        List<PersistenceUnitDescriptor> persistenceUnits = persistenceXmlDescriptor.getContains();
        assertThat(persistenceUnits, hasItem(PersistenceUnitMatcher.persistenceUnitDescriptor("persistence-unit")));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "jpa2:ValidationModeMustBeExplicitlySpecified" if
     * it is not set.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void validationModeNotSpecified() throws Exception {
        scanClassPathDirectory(new File(getClassesDirectory(JpaEntity.class), "minimal"));
        assertThat(validateConstraint("jpa2:ValidationModeMustBeExplicitlySpecified").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportWriter.getConstraintResults().values());
        Matcher<Iterable<? super Result<Constraint>>> matcher = hasItem(result(constraint("jpa2:ValidationModeMustBeExplicitlySpecified")));
        assertThat(constraintViolations, matcher);
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> constraintResult = constraintViolations.get(0);
        assertThat(constraintResult.isEmpty(), equalTo(false));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "jpa2:ValidationModeMustBeExplicitlySpecified" if
     * it is set to AUTO.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void validationModeAuto() throws Exception {
        scanClassPathDirectory(new File(getClassesDirectory(JpaEntity.class), "full"));
        assertThat(validateConstraint("jpa2:ValidationModeMustBeExplicitlySpecified").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportWriter.getConstraintResults().values());
        Matcher<Iterable<? super Result<Constraint>>> matcher = hasItem(result(constraint("jpa2:ValidationModeMustBeExplicitlySpecified")));
        assertThat(constraintViolations, matcher);
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> constraintResult = constraintViolations.get(0);
        assertThat(constraintResult.isEmpty(), equalTo(false));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "jpa2:ValidationModeMustBeExplicitlySpecified"
     * for values NONE and CALLBACK.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void validationModeSpecified() throws Exception {
        scanClassPathDirectory(new File(getClassesDirectory(JpaEntity.class), "validationmode"));
        assertThat(validateConstraint("jpa2:ValidationModeMustBeExplicitlySpecified").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportWriter.getConstraintResults().values());
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> constraintResult = constraintViolations.get(0);
        assertThat(constraintResult.isEmpty(), equalTo(true));
        store.commitTransaction();
    }
    
    /**
     * Verifies a unique NamedQuery with property.
     * @param relationCount The number of :DEFINES relations to named query nodes.
     * @param withQueryCount The number of nodes with the query attribute.
     * @param withoutQueryCount The number of nodes without the query attribute.
     */
    private void verifyUniqueRelation(int relationCount, int withQueryCount, int withoutQueryCount) {
    	List<Object> column = query("MATCH ()-[r:DEFINES]->(:Jpa:NamedQuery) RETURN r").getColumn("r");
    	if (relationCount == 0) {
    		assertNull(column);
    	} else {
    		assertThat(column.size(), equalTo(relationCount));
    	}
    	assertThat(query("MATCH (q:Jpa:NamedQuery {prop: 'value'}) RETURN q").getColumn("q").size(), equalTo(1));
    	column = query("MATCH ()-[:DEFINES]->(q:Jpa:NamedQuery {name: 'testQuery', query: 'SELECT e FROM JpaEntity e'}) RETURN q").getColumn("q");
    	if (withQueryCount == 0) {
    		assertNull(column);
    	} else {
    		assertThat(column.size(), equalTo(withQueryCount));
    	}
    	column = query("MATCH (q:Jpa:NamedQuery {name: 'testQuery'}) WHERE q.query IS NULL RETURN q").getColumn("q");
    	if (withoutQueryCount == 0) {
    		assertNull(column);
    	} else {
    		assertThat(column.size(), equalTo(withoutQueryCount));
    	}
    }
}
