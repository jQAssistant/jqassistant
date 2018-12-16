package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.IOException;
import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.PropertyFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.ValueDescriptorMatcher.valueDescriptor;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Contains tests regarding property files.
 */
public class PropertyFileIT extends AbstractJavaPluginIT {

    /**
     * Verifies that property files are scanned.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void propertyFile() throws IOException {
        scanClassPathResource(JavaScope.CLASSPATH, "/META-INF/test.properties");
        store.beginTransaction();
        List<PropertyFileDescriptor> propertyFileDescriptors = query("MATCH (p:Properties:File) RETURN p").getColumn("p");
        assertThat(propertyFileDescriptors.size(), equalTo(1));
        PropertyFileDescriptor propertyFileDescriptor = propertyFileDescriptors.get(0);
        Matcher<? super PropertyDescriptor> valueMatcher = valueDescriptor("foo", equalTo("bar"));
        assertThat(propertyFileDescriptor.getFileName(), endsWith("/META-INF/test.properties"));
        assertThat(propertyFileDescriptor.getProperties(), hasItem(valueMatcher));
        store.commitTransaction();
    }

    /**
     * Verifies that invalid property files don't make the scanner fail.
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void invalidPropertyFile() throws IOException {
        scanClassPathResource(JavaScope.CLASSPATH, "/META-INF/invalid.properties");
        store.beginTransaction();
        List<PropertyFileDescriptor> propertyFileDescriptors = query("MATCH (p:Properties:File) RETURN p").getColumn("p");
        assertThat(propertyFileDescriptors.size(), equalTo(1));
        PropertyFileDescriptor propertyFileDescriptor = propertyFileDescriptors.get(0);
        assertThat(propertyFileDescriptor.getFileName(), endsWith("/META-INF/invalid.properties"));
        assertThat(propertyFileDescriptor.getProperties().size(), equalTo(0));
        store.commitTransaction();
    }
}
