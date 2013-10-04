package com.buschmais.jqassistant.plugin.java.test.scanner;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PrimitiveValueDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertiesDescriptor;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.buschmais.jqassistant.plugin.java.test.matcher.ValueDescriptorMatcher.valueDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Contains tests regarding property files.
 */
public class PropertyFileIT extends AbstractPluginIT {

    /**
     * Verifies that property files are scanned.
     *
     * @throws java.io.IOException If the test fails.
     */
    @Test
    public void propertyFile() throws IOException {
        scanURLs(PropertyFileIT.class.getResource("/META-INF/test.properties"));
        List<PropertiesDescriptor> propertiesDescriptors = query("MATCH (p:PROPERTIES) RETURN p").getColumn("p");
        assertThat(propertiesDescriptors.size(), equalTo(1));
        PropertiesDescriptor propertiesDescriptor = propertiesDescriptors.get(0);
        Matcher<? super PrimitiveValueDescriptor> valueMatcher = (Matcher<? super PrimitiveValueDescriptor>) valueDescriptor("foo", equalTo("bar"));
        assertThat(propertiesDescriptor.getProperties(), hasItem(valueMatcher));
    }
}
