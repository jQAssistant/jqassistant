package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.ServiceLoader;

import org.junit.Before;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.java.api.model.ServiceLoaderDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.serviceloader.OuterClass;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.serviceloader.Service;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.serviceloader.ServiceImpl;

/**
 * Contains tests regarding service loader descriptors.
 */
public class ServiceLoaderIT extends AbstractJavaPluginIT {

    @Before
    public void verifyServiceLoader() {
        ServiceLoader<Service> services = ServiceLoader.load(Service.class);
        assertThat(services, hasItem(any(ServiceImpl.class)));
        assertThat(services, hasItem(any(OuterClass.InnerClassServiceImpl.class)));
    }

    /**
     * Verifies that service loader descriptor files are scanned.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void resolvableServiceImplementation() throws IOException {
        scanClasses(Service.class, ServiceImpl.class, OuterClass.InnerClassServiceImpl.class);
        scanClassPathResource(JavaScope.CLASSPATH, "/META-INF/services/" + Service.class.getName());
        verifyServiceLoaderDescriptor();
    }

    /**
     * Verifies that service loader descriptor files are scanned.
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void unresolvableServiceImplementation() throws IOException {
        scanClassPathResource(JavaScope.CLASSPATH, "/META-INF/services/" + Service.class.getName());
        verifyServiceLoaderDescriptor();
    }

    /**
     * Verifies the expected service loader descriptor and its content.
     */
    private void verifyServiceLoaderDescriptor() {
        store.beginTransaction();
        List<ServiceLoaderDescriptor> serviceLoaderDescriptors = query("MATCH (sd:ServiceLoader:File) RETURN sd").getColumn("sd");
        assertThat(serviceLoaderDescriptors.size(), equalTo(1));

        ServiceLoaderDescriptor serviceLoaderDescriptor = serviceLoaderDescriptors.get(0);
        assertThat(serviceLoaderDescriptor.getFileName(), endsWith("/META-INF/services/" + Service.class.getName()));
        assertThat(serviceLoaderDescriptor.getType(), typeDescriptor(Service.class));
        List<TypeDescriptor> serviceTypes = serviceLoaderDescriptor.getContains();
        assertThat(serviceTypes.size(), equalTo(2));
        assertThat(serviceTypes, hasItem(typeDescriptor(ServiceImpl.class)));
        assertThat(serviceTypes, hasItem(typeDescriptor(OuterClass.InnerClassServiceImpl.class)));

        store.commitTransaction();
    }
}
