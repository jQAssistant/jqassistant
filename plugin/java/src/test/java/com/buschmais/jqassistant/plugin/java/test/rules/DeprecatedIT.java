package com.buschmais.jqassistant.plugin.java.test.rules;

import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.deprecated.DeprecatedType;

/**
 * Tests for the concept java:Deprecated.
 */
@SuppressWarnings("deprecation")
public class DeprecatedIT extends AbstractPluginIT {

    /**
     * Verifies the concept "java:Deprecated".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws AnalyzerException
     *             If the test fails.
     */
    @Test
    public void deprecated() throws IOException, AnalyzerException, NoSuchMethodException, NoSuchFieldException {
        scanClasses(DeprecatedType.class);
        String packageInfoName = DeprecatedType.class.getPackage().getName() + ".package-info";
        scanURLs(DeprecatedIT.class.getResource("/" + packageInfoName.replaceAll("\\.", "/") + ".class"));
        applyConcept("java:Deprecated");
        store.beginTransaction();
        assertThat(query("MATCH (element:TYPE:CLASS:DEPRECATED) RETURN element").getColumn("element"), hasItem(typeDescriptor(DeprecatedType.class)));
        assertThat(query("MATCH (element:TYPE:INTERFACE:DEPRECATED) RETURN element as element").getColumn("element"), hasItem(typeDescriptor(packageInfoName)));
        assertThat(query("MATCH (element:FIELD:DEPRECATED) RETURN element").getColumn("element"), hasItem(fieldDescriptor(DeprecatedType.class, "value")));
        assertThat(query("MATCH (element:METHOD:DEPRECATED) RETURN element").getColumn("element"), hasItem(methodDescriptor(DeprecatedType.class, "getValue")));
        assertThat(query("MATCH (element:METHOD:DEPRECATED) RETURN element").getColumn("element"),
                hasItem(methodDescriptor(DeprecatedType.class, "setValue", int.class)));
        assertThat(query("MATCH (element:PARAMETER:DEPRECATED) RETURN element.INDEX as index").getColumn("index"), hasItem(equalTo(0)));
        store.commitTransaction();
    }
}
