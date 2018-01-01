package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.metric.CyclomaticComplexityType;

import org.junit.Test;

import static java.lang.Integer.valueOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class CyclomaticComplexityIT extends AbstractJavaPluginIT {

    @Test
    public void cyclomaticComplexity() throws IOException {
        Map<String, Integer> expectedComplexities = new HashMap<>();
        expectedComplexities.put("<init>", valueOf(1));
        expectedComplexities.put("ifStatement", valueOf(2));
        expectedComplexities.put("caseStatement", valueOf(3));
        scanClasses(CyclomaticComplexityType.class);
        store.beginTransaction();
        List<MethodDescriptor> methods = query("match (:Class)-[:DECLARES]->(m:Method) return m").getColumn("m");
        assertThat(methods.size(), equalTo(3));
        for (MethodDescriptor methodDescriptor : methods) {
            String name = methodDescriptor.getName();
            int cyclomaticComplexity = methodDescriptor.getCyclomaticComplexity();
            Integer expectedComplexity = expectedComplexities.get(name);
            assertThat(expectedComplexity, notNullValue());
            assertThat(cyclomaticComplexity, equalTo(expectedComplexity));
        }
        store.commitTransaction();
    }


}
