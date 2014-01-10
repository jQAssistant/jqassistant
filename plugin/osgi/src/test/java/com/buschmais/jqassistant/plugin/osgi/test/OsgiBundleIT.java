package com.buschmais.jqassistant.plugin.osgi.test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.plugin.osgi.test.api.data.Request;
import com.buschmais.jqassistant.plugin.osgi.test.api.service.Service;
import com.buschmais.jqassistant.plugin.osgi.test.impl.Activator;
import com.buschmais.jqassistant.plugin.osgi.test.impl.ServiceImpl;

import org.junit.Test;

import javax.validation.constraints.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static com.buschmais.jqassistant.plugin.java.test.matcher.PackageDescriptorMatcher.packageDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

/**
 * Contains tests regarding manifest files.
 */
public class OsgiBundleIT extends AbstractPluginIT {

    /**
     * Verifies the concept "osgi-bundle:Bundle".
     *
     * @throws IOException       If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void bundle() throws IOException, AnalyzerException {
        scanURLs(getManifestUrls());
        applyConcept("osgi-bundle:Bundle");
        store.beginTransaction();
        assertThat(query("MATCH (bundle:OSGI:BUNDLE) WHERE bundle.BUNDLESYMBOLICNAME='com.buschmais.jqassistant.plugin.osgi.test' RETURN bundle").getColumn("bundle").size(), equalTo(1));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "osgi-bundle:ExportPackage".
     *
     * @throws IOException       If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void exportedPackages() throws IOException, AnalyzerException {
        scanURLs(getManifestUrls());
        scanClassesDirectory(Service.class);
        applyConcept("osgi-bundle:ExportPackage");
        store.beginTransaction();
        List<PackageDescriptor> packages = query("MATCH (b:OSGI:BUNDLE)-[:EXPORTS]->(p:PACKAGE) RETURN p").getColumn("p");
        assertThat(packages.size(), equalTo(2));
        assertThat(packages, hasItems(packageDescriptor(Request.class.getPackage()), packageDescriptor(Service.class.getPackage())));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "osgi-bundle:ImportPackage".
     *
     * @throws IOException       If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void importedPackages() throws IOException, AnalyzerException {
        scanURLs(getManifestUrls());
        scanClassesDirectory(Service.class);
        applyConcept("osgi-bundle:ImportPackage");
        store.beginTransaction();
        List<PackageDescriptor> packages = query("MATCH (b:OSGI:BUNDLE)-[:IMPORTS]->(p:PACKAGE) RETURN p").getColumn("p");
        assertThat(packages.size(), equalTo(1));
        assertThat(packages, hasItems(packageDescriptor(NotNull.class.getPackage())));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "osgi-bundle:PublicClassesWithUninvokedMethodsInUnexportedPackages".
     *
     * @throws IOException       If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void publicClassesWithUninvokedMethodsInUnexportedPackages() throws IOException, AnalyzerException {
        scanURLs(getManifestUrls());
        scanClassesDirectory(Service.class);
        applyConcept("osgi-bundle:PublicClassesWithUninvokedMethodsInUnexportedPackages");
        store.beginTransaction();
        List<TypeDescriptor> classes = query("MATCH (c:TYPE:CLASS:UNUSED) RETURN c").getColumn("c");
        assertThat(classes.size(), equalTo(2));
        assertThat(classes, hasItems(typeDescriptor(Activator.class), typeDescriptor(ServiceImpl.class)));
        store.commitTransaction();
    }

    /**
     * Retrieves the URL of the test MANIFEST.MF file.
     *
     * @return The URL.
     * @throws IOException If a problem occurs.
     */
    private URL getManifestUrls() throws IOException {
        return new File(OsgiBundleIT.class.getResource("/").getFile(), "/META-INF/MANIFEST.MF").toURI().toURL();
    }
}
