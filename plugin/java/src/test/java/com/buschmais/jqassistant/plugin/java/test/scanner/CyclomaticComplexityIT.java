package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.metric.CyclomaticComplexityType;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CyclomaticComplexityIT extends AbstractJavaPluginIT {

    @Test
    void cyclomaticComplexity() {
        Map<String, Integer> expectedComplexities = new HashMap<>();
        expectedComplexities.put("<init>", 1);
        expectedComplexities.put("ifStatement", 2);
        expectedComplexities.put("caseStatement", 4);
        expectedComplexities.put("tryCatch", 3);
        expectedComplexities.put("nestedTryCatch", 5);
        expectedComplexities.put("tryWithResources", 3);
        scanClasses(CyclomaticComplexityType.class);
        store.beginTransaction();
        List<MethodDescriptor> methods = query("match (:Class)-[:DECLARES]->(m:Method) return m").getColumn("m");
        assertThat(methods.size()).isEqualTo(expectedComplexities.size());
        for (MethodDescriptor methodDescriptor : methods) {
            String name = methodDescriptor.getName();
            int cyclomaticComplexity = methodDescriptor.getCyclomaticComplexity();
            Integer expectedComplexity = expectedComplexities.get(name);
            assertThat(expectedComplexity).as("Expecting a CC for method " + name).isNotNull();
            assertThat(cyclomaticComplexity).as("Invalid CC for method " + name).isEqualTo(expectedComplexity);
        }
        store.commitTransaction();
    }

}
