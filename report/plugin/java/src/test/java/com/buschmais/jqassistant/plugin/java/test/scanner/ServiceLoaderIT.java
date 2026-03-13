package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.File;
import java.util.List;
import java.util.ServiceLoader;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ServiceLoaderDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.serviceloader.OuterClass;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.serviceloader.Service;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.serviceloader.ServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

/**
 * Contains tests regarding service loader descriptors.
 */
class ServiceLoaderIT extends AbstractJavaPluginIT {

    @BeforeEach
    void verifyServiceLoader() {
        ServiceLoader<Service> services = ServiceLoader.load(Service.class);
        assertThat(services, hasItem(any(ServiceImpl.class)));
        assertThat(services, hasItem(any(OuterClass.InnerClassServiceImpl.class)));
    }

    /**
     * Verifies that service loader descriptor files are scanned.
     *
     */
    @Test
    void resolvableServiceImplementation() {
        scanClasses(Service.class, ServiceImpl.class, OuterClass.InnerClassServiceImpl.class);
        scanClassPathResource(JavaScope.CLASSPATH, "/META-INF/services/" + Service.class.getName());
        verifyServiceLoaderDescriptor();
    }

    /**
     * Verifies that service loader descriptor files are scanned.
     *
     */
    @Test
    void unresolvableServiceImplementation() {
        scanClassPathResource(JavaScope.CLASSPATH, "/META-INF/services/" + Service.class.getName());
        verifyServiceLoaderDescriptor();
    }

    /**
     * Verifies that any files not representing service descriptors are ignored.
     *
     */
    @Test
    void invalidDescriptor() {
        File file = getClassesDirectory(ServiceLoaderIT.class);
        final File propsFile = new File(file, "META-INF/test.properties");
        final String path = "META-INF/services/test.properties";
        store.beginTransaction();
        JavaClassesDirectoryDescriptor artifactDescriptor = getArtifactDescriptor("a1");
        execute(artifactDescriptor, new ScanClassPathOperation() {
            @Override
            public List<FileDescriptor> scan(JavaArtifactFileDescriptor artifact, Scanner scanner) {
                return singletonList(scanner.scan(propsFile, path, JavaScope.CLASSPATH));
            }
        }, getScanner());
        List<ServiceLoaderDescriptor> s = query("MATCH (s:ServiceLoader:Properties:File) RETURN s").getColumn("s");
        assertThat(s.size()).isEqualTo(1);
        ServiceLoaderDescriptor serviceLoaderDescriptor = s.get(0);
        assertThat(serviceLoaderDescriptor.getFileName()).isEqualTo(path);
        store.commitTransaction();
    }


    /**
     * Verifies the expected service loader descriptor and its content.
     */
    private void verifyServiceLoaderDescriptor() {
        store.beginTransaction();
        List<ServiceLoaderDescriptor> serviceLoaderDescriptors = query("MATCH (sd:ServiceLoader:File) RETURN sd").getColumn("sd");
        assertThat(serviceLoaderDescriptors.size()).isEqualTo(1);

        ServiceLoaderDescriptor serviceLoaderDescriptor = serviceLoaderDescriptors.get(0);
        assertThat(serviceLoaderDescriptor.getFileName()).endsWith("/META-INF/services/" + Service.class.getName());
        assertThat(serviceLoaderDescriptor.getType(), typeDescriptor(Service.class));
        List<TypeDescriptor> serviceTypes = serviceLoaderDescriptor.getContains();
        assertThat(serviceTypes.size()).isEqualTo(2);
        assertThat(serviceTypes, hasItem(typeDescriptor(ServiceImpl.class)));
        assertThat(serviceTypes, hasItem(typeDescriptor(OuterClass.InnerClassServiceImpl.class)));

        store.commitTransaction();
    }
}
