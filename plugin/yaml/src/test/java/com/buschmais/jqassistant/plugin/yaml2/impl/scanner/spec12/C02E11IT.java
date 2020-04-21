package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import java.util.List;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLMapDescriptor;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.yaml2.helper.YMLPluginAssertions.assertThat;

class C02E11IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/spec-examples/c2-e11-mapping-between-sequences.yaml";

    @Override
    protected String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }

    @Test
    void theMapHasOnlyTwoComplexKeys() {
        readSourceDocument();

        String query = "MATCH (m:Map:Yaml)" +
                       "return m";

        TestResult testResult = query(query);

        List<YMLMapDescriptor> maps = testResult.getColumn("m");
        assertThat(maps).hasSize(1);

        YMLMapDescriptor mapDescriptor = maps.get(0);

        assertThat(mapDescriptor).hasNoSimpleKeys();
        assertThat(mapDescriptor).hasComplexKeys(2);
    }

    @Test
    void cypherComplexKeyHasOutGoingRelationshipHasComplexKey() {
        readSourceDocument();

        String query = "MATCH (ck:Key:Complex:Yaml)-[:HAS_COMPLEX_KEY]->(ckv) " +
                       "RETURN ck";

        List<YMLDescriptor> ck = query(query).getColumn("ck");

        assertThat(ck).hasSize(2);
    }

    @Test
    void cypherComplexKeyHasOutgoingRelationshipHasValue() {
        readSourceDocument();

        String query = "MATCH (ck:Key:Complex:Yaml)-[:HAS_VALUE]->(v) " +
                       "RETURN ck";

        List<YMLDescriptor> ck = query(query).getColumn("ck");

        assertThat(ck).hasSize(2);
    }

    @Test
    void cypherAComplexKeyHasTheCorrectLabels() {
        readSourceDocument();

        String checkExistingOfCompleyKeysQuery = "MATCH (ck:Complex:Key) " +
                                                 "RETURN ck";

        List<Object> existenceResult = query(checkExistingOfCompleyKeysQuery).getColumn("ck");

        assertThat(existenceResult).hasSize(2);

        String findKeysWithUnwantedLabels = "WITH [\"Yaml\", \"Key\", \"Complex\"] AS expected " +
                                            "MATCH (ck:Complex:Key) " +
                                            "WITH labels(ck) AS labels " +
                                            "WHERE labels <> expected " +
                                            "RETURN labels";

        TestResult query = query(findKeysWithUnwantedLabels);

        assertThat(query.getColumns()).isEmpty();
    }

    @Test
    void cypherTheFirstComplexKeyValueIsCorrect() {
        readSourceDocument();

        String query = "MATCH (i1:Scalar {value: \"Detroit Tigers\"})" +
                       "       <-[:HAS_ITEM]-(ckv)" +
                       "       -[:HAS_ITEM]->(i2:Scalar {value: \"Chicago cubs\"}) " +
                       "WHERE (ckv)<-[:HAS_COMPLEX_KEY]-() " +
                       "RETURN ckv";

        List<YMLDescriptor> result = query(query).getColumn("ckv");

        assertThat(result).hasSize(1);
    }

    @Test
    void cypherTheKeyOfTheCompleyKeyHasTheLabelComplexKeyValue() {
        readSourceDocument();

        String query = "MATCH (i1:Scalar {value: \"Detroit Tigers\"})" +
                       "       <-[:HAS_ITEM]-(ckv:ComplexKeyValue)" +
                       "       -[:HAS_ITEM]->(i2:Scalar {value: \"Chicago cubs\"}) " +
                       "WHERE (ckv)<-[:HAS_COMPLEX_KEY]-() " +
                       "RETURN ckv";

        List<YMLDescriptor> result = query(query).getColumn("ckv");

        assertThat(result).hasSize(1);
    }

    // @TestStore(type = TestStore.Type.FILE)
    @Test
    void cypherTheValueOfTheCompleyKeyHasTheLabelComplexKeyValue() {
        readSourceDocument();

        String query = "MATCH (i1:Scalar {value: \"Detroit Tigers\"})" +
                       "       <-[:HAS_ITEM]-(ckv:ComplexKeyValue)" +
                       "       -[:HAS_ITEM]->(i2:Scalar {value: \"Chicago cubs\"}), " +
                       "       (ckv)<-[:HAS_COMPLEX_KEY]-(key) " +
                       "MATCH (key)-[:HAS_VALUE]->(v:Yaml:Value) " +
                       "RETURN v";

        System.out.println(query);
        List<YMLDescriptor> result = query(query).getColumn("v");

        assertThat(result).hasSize(1);
    }
}
