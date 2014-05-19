package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ServiceLoaderDescriptor;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.serviceloader.Service;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.serviceloader.ServiceImpl;

/**
 * Contains tests regarding service loader descriptors.
 */
public class ServiceLoaderIT extends AbstractPluginIT {

    /**
     * Verifies that service loader descriptor files are scanned.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void manifestFile() throws IOException {
        scanClasses(Service.class, ServiceImpl.class);
        scanURLs(ServiceLoaderIT.class.getResource("/META-INF/services/" + Service.class.getName()));
        store.beginTransaction();
        List<ServiceLoaderDescriptor> serviceLoaderDescriptors = query("MATCH (sd:ServiceLoader:File) RETURN sd").getColumn("sd");
        assertThat(serviceLoaderDescriptors.size(), equalTo(1));

        ServiceLoaderDescriptor serviceLoaderDescriptor = serviceLoaderDescriptors.get(0);
        assertThat(serviceLoaderDescriptor.getFileName(), endsWith("/META-INF/services/" + Service.class.getName()));
        assertThat(serviceLoaderDescriptor.getType(), typeDescriptor(Service.class));
        assertThat(serviceLoaderDescriptor.getContains(), hasItem(typeDescriptor(ServiceImpl.class)));

        store.commitTransaction();
    }
}
