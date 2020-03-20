package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import java.util.List;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12.AbstractYAMLPluginIT;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.yaml2.helper.YMLPluginAssertions.assertThat;

public class ComplexKeyIT extends AbstractYAMLPluginIT {

    @Test
    void cypherAScalarCanBeUsedAsKeyValueForAComplexKey() {
        /*
         * A scalar can be used as complex key value but it seems
         * not to be possible to identify it as complex key as
         * the parser does not provide any information how the mapping
         * is defined. Currently a complex key can be identified if
         * the parser emits the start of a sequence or the start of a map
         * at the position of the key.
         */
        readSourceDocument("/complexkey/scalar-as-keyvalue.yml");

        String query = "MATCH (key:Key:Yaml), " +
                       "      (value:Value:Scalar) " +
                       "RETURN key, value";

        TestResult testResult = query(query);

        List<YMLDescriptor> keys = testResult.getColumn("key");
        List<YMLDescriptor> values = testResult.getColumn("value");

        assertThat(keys).hasSize(1);
        assertThat(values).hasSize(1);
    }


    @Test
    void cypherAMapCanBeUsedAsKeyValueForAComplexKey() {
        readSourceDocument("/complexkey/map-as-keyvalue.yml");

        String query = "MATCH (key:Key:Yaml:Complex), " +
                       "      (complexKeyValue:ComplexKeyValue:Yaml:Map) " +
                       "RETURN key, complexKeyValue";

        TestResult testResult = query(query);

        List<YMLDescriptor> keys = testResult.getColumn("key");
        List<YMLDescriptor> values = testResult.getColumn("complexKeyValue");

        assertThat(keys).hasSize(1);
        assertThat(values).hasSize(1);
    }

    @Test
    void cypherASequenceCanBeUsedAsKeyValueForAComplexKey() {
        readSourceDocument("/complexkey/sequence-as-keyvalue.yml");

        String query = "MATCH (key:Key:Yaml:Complex), " +
                       "      (complexKeyValue:ComplexKeyValue:Yaml:Sequence) " +
                       "RETURN key, complexKeyValue";

        TestResult testResult = query(query);

        List<YMLDescriptor> keys = testResult.getColumn("key");
        List<YMLDescriptor> values = testResult.getColumn("complexKeyValue");

        assertThat(keys).hasSize(1);
        assertThat(values).hasSize(1);
    }
}
