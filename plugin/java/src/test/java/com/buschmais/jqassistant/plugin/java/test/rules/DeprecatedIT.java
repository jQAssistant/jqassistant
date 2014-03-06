package com.buschmais.jqassistant.plugin.java.test.rules;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher;
import com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher;
import com.buschmais.jqassistant.plugin.java.test.set.rules.deprecated.DeprecatedType;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Tests for the concept java:Deprecated.
 */
public class DeprecatedIT extends AbstractPluginIT {

    /**
     * Verifies the concept "java:Deprecated".
     *
     * @throws java.io.IOException                                           If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalyzerException If the test fails.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void deprecated() throws IOException, AnalyzerException, NoSuchMethodException, NoSuchFieldException {
        scanClasses(DeprecatedType.class);
        scanURLs("/com/buschmais/jqassistant/plugin/java/test/set/rules/deprecated/package-info.class");
        applyConcept("java:Deprecated");
        store.beginTransaction();
        assertThat(query("MATCH (element:TYPE:DEPRECATED) RETURN element").getColumn("element"), hasItem(TypeDescriptorMatcher.typeDescriptor(DeprecatedType.class)));
        assertThat(query("MATCH (element:FIELD:DEPRECATED) RETURN element").getColumn("element"), hasItem(fieldDescriptor(DeprecatedType.class, "value")));
        assertThat(query("MATCH (element:METHOD:DEPRECATED) RETURN element").getColumn("element"), hasItem(methodDescriptor(DeprecatedType.class, "getValue")));
        assertThat(query("MATCH (element:METHOD:DEPRECATED) RETURN element").getColumn("element"), hasItem(methodDescriptor(DeprecatedType.class, "setValue", int.class)));
        assertThat(query("MATCH (element:PARAMETER:DEPRECATED) RETURN element.INDEX as index").getColumn("index"), hasItem(equalTo(0)));
        store.commitTransaction();
    }
}
