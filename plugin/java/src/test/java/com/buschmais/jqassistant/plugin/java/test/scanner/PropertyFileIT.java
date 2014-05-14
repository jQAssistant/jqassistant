package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.ValueDescriptorMatcher.valueDescriptor;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertyFileDescriptor;

/**
 * Contains tests regarding property files.
 */
public class PropertyFileIT extends AbstractPluginIT {

    /**
     * Verifies that property files are scanned.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void propertyFile() throws IOException {
        scanURLs(PropertyFileIT.class.getResource("/META-INF/test.properties"));
        store.beginTransaction();
        List<PropertyFileDescriptor> propertyFileDescriptors = query("MATCH (p:Properties:File) RETURN p").getColumn("p");
        assertThat(propertyFileDescriptors.size(), equalTo(1));
        PropertyFileDescriptor propertyFileDescriptor = propertyFileDescriptors.get(0);
        Matcher<? super PropertyDescriptor> valueMatcher = valueDescriptor("foo", equalTo("bar"));
        assertThat(propertyFileDescriptor.getFileName(), endsWith("/META-INF/test.properties"));
        assertThat(propertyFileDescriptor.getProperties(), hasItem(valueMatcher));
        store.commitTransaction();
    }
}
