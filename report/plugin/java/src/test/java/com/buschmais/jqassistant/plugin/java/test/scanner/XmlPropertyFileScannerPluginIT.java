package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.IOException;
import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.PropertyFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.PropertyDescriptorMatcher.propertyDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.PropertyFileDescriptorMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;

class XmlPropertyFileScannerPluginIT extends AbstractJavaPluginIT {

    @AfterEach
    void commitTransaction() {
        if (store.hasActiveTransaction()) {
            store.commitTransaction();
        }
    }

    @Test
    void emptyPropertiesFileDoesNotHaveProperties() throws IOException {
        scanClassPathResource(JavaScope.CLASSPATH, "/set/scanner/propertyfiles/properties-empty.xml");
        store.beginTransaction();
        List<FileDescriptor> fileDescriptors =
            query("MATCH (f:File:Xml:Properties) RETURN f").getColumn("f");

        assertThat(fileDescriptors).isNotNull();
        assertThat(fileDescriptors).hasSize(1);

        PropertyFileDescriptor propertyFileDescriptor = (PropertyFileDescriptor)fileDescriptors.get(0);
        assertThat(propertyFileDescriptor, hasNoProperties());
    }

    @Test
    void propertiesFileHasProperties() throws IOException {
        scanClassPathResource(JavaScope.CLASSPATH, "/set/scanner/propertyfiles/properties-2-props.xml");
        store.beginTransaction();
        List<FileDescriptor> fileDescriptors =
            query("MATCH (f:File:Xml:Properties) RETURN f").getColumn("f");

        assertThat(fileDescriptors).isNotNull();
        assertThat(fileDescriptors).hasSize(1);

        PropertyFileDescriptor propertyFileDescriptor = (PropertyFileDescriptor)fileDescriptors.get(0);

        assertThat(propertyFileDescriptor, hasProperties(2));
        assertThat(propertyFileDescriptor, containsProperties(propertyDescriptor("keyA", "valueA"),
                                                              propertyDescriptor("keyB", "valueB")));
    }


}
