package com.buschmais.jqassistant.plugin.java.test.scanner;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.PropertyFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.buschmais.jqassistant.plugin.java.test.matcher.PropertyDescriptorMatcher.propertyDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.PropertyFileDescriptorMatchers.containsProperties;
import static com.buschmais.jqassistant.plugin.java.test.matcher.PropertyFileDescriptorMatchers.hasNoProperties;
import static com.buschmais.jqassistant.plugin.java.test.matcher.PropertyFileDescriptorMatchers.hasProperties;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class XmlPropertyFileScannerPluginIT extends AbstractJavaPluginIT {

    @After
    public void commitTransaction() {
        if (store.hasActiveTransaction()) {
            store.commitTransaction();
        }
    }

    @Test
    public void emptyPropertiesFileDoesNotHaveProperties() throws IOException {
        scanClassPathResource(JavaScope.CLASSPATH, "/set/scanner/propertyfiles/properties-empty.xml");
        store.beginTransaction();
        List<FileDescriptor> fileDescriptors =
            query("MATCH (f:File:Xml:Properties) RETURN f").getColumn("f");

        assertThat(fileDescriptors, CoreMatchers.notNullValue());
        assertThat(fileDescriptors, hasSize(1));

        PropertyFileDescriptor propertyFileDescriptor = (PropertyFileDescriptor)fileDescriptors.get(0);
        assertThat(propertyFileDescriptor, hasNoProperties());
    }

    @Test
    public void propertiesFileHasProperties() throws IOException {
        scanClassPathResource(JavaScope.CLASSPATH, "/set/scanner/propertyfiles/properties-2-props.xml");
        store.beginTransaction();
        List<FileDescriptor> fileDescriptors =
            query("MATCH (f:File:Xml:Properties) RETURN f").getColumn("f");

        assertThat(fileDescriptors, CoreMatchers.notNullValue());
        assertThat(fileDescriptors, hasSize(1));

        PropertyFileDescriptor propertyFileDescriptor = (PropertyFileDescriptor)fileDescriptors.get(0);

        assertThat(propertyFileDescriptor, hasProperties(2));
        assertThat(propertyFileDescriptor, containsProperties(propertyDescriptor("keyA", "valueA"),
                                                              propertyDescriptor("keyB", "valueB")));
    }


}