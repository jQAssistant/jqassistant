package com.buschmais.jqassistant.plugin.osgi.test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.plugin.osgi.test.api.data.Request;
import com.buschmais.jqassistant.plugin.osgi.test.api.data.Response;
import com.buschmais.jqassistant.plugin.osgi.test.api.service.Service;
import com.buschmais.jqassistant.plugin.osgi.test.impl.Activator;
import com.buschmais.jqassistant.plugin.osgi.test.impl.ServiceImpl;
import com.buschmais.jqassistant.plugin.osgi.test.impl.a.UsedPublicClass;
import com.buschmais.jqassistant.plugin.osgi.test.impl.b.UnusedPublicClass;
import org.hamcrest.Matcher;
import org.junit.Test;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static com.buschmais.jqassistant.core.analysis.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ResultMatcher.result;
import static com.buschmais.jqassistant.plugin.java.test.matcher.PackageDescriptorMatcher.packageDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsMapContaining.hasValue;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
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
        scanURLs(getManifestUrl());
        applyConcept("osgi-bundle:Bundle");
        store.beginTransaction();
        assertThat(query("MATCH (bundle:OSGI:BUNDLE) WHERE bundle.BUNDLESYMBOLICNAME='com.buschmais.jqassistant.plugin.osgi.test' and bundle.BUNDLEVERSION='0.1.0' RETURN bundle").getColumn("bundle").size(), equalTo(1));
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
        scanURLs(getManifestUrl());
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
        scanURLs(getManifestUrl());
        scanClassesDirectory(Service.class);
        applyConcept("osgi-bundle:ImportPackage");
        store.beginTransaction();
        List<PackageDescriptor> packages = query("MATCH (b:OSGI:BUNDLE)-[:IMPORTS]->(p:PACKAGE) RETURN p").getColumn("p");
        assertThat(packages.size(), equalTo(1));
        assertThat(packages, hasItems(packageDescriptor(NotNull.class.getPackage())));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "osgi-bundle:Activator".
     *
     * @throws IOException       If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void activator() throws IOException, AnalyzerException {
        scanURLs(getManifestUrl());
        scanClassesDirectory(Service.class);
        applyConcept("osgi-bundle:Activator");
        store.beginTransaction();
        List<TypeDescriptor> activators = query("MATCH (a:CLASS)-[:ACTIVATES]->(b:OSGI:BUNDLE) RETURN a").getColumn("a");
        assertThat(activators.size(), equalTo(1));
        assertThat(activators, hasItems(typeDescriptor(Activator.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "osgi-bundle:InternalType".
     *
     * @throws IOException       If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void internalType() throws IOException, AnalyzerException {
        scanURLs(getManifestUrl());
        scanClassesDirectory(Service.class);
        removeTestClass();
        applyConcept("osgi-bundle:InternalType");
        store.beginTransaction();
        List<TypeDescriptor> internalTypes = query("MATCH (t:TYPE:INTERNAL) RETURN t").getColumn("t");
        assertThat(internalTypes, hasItems(typeDescriptor(Activator.class), typeDescriptor(UsedPublicClass.class), typeDescriptor(UnusedPublicClass.class), typeDescriptor(ServiceImpl.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "osgi-bundle:InternalTypeMustNotBePublic".
     *
     * @throws IOException       If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void internalTypeMustNotBePublic() throws IOException, AnalyzerException {
        scanURLs(getManifestUrl());
        scanClassesDirectory(Service.class);
        removeTestClass();
        validateConstraint("osgi-bundle:InternalTypeMustNotBePublic");
        store.beginTransaction();
        Matcher<Constraint> constraintMatcher = constraint("osgi-bundle:InternalTypeMustNotBePublic");
        List<Result<Constraint>> constraintViolations = reportWriter.getConstraintViolations();
        assertThat(constraintViolations, hasItem(result(constraintMatcher, hasItem(hasValue(typeDescriptor(UnusedPublicClass.class))))));
        assertThat(constraintViolations, hasItem(result(constraintMatcher, hasItem(hasValue(typeDescriptor(ServiceImpl.class))))));
        assertThat(constraintViolations, not(hasItem(result(constraintMatcher, hasItem(hasValue(typeDescriptor(Request.class)))))));
        assertThat(constraintViolations, not(hasItem(result(constraintMatcher, hasItem(hasValue(typeDescriptor(Response.class)))))));
        assertThat(constraintViolations, not(hasItem(result(constraintMatcher, hasItem(hasValue(typeDescriptor(Service.class)))))));
        assertThat(constraintViolations, not(hasItem(result(constraintMatcher, hasItem(hasValue(typeDescriptor(UsedPublicClass.class)))))));
        assertThat(constraintViolations, not(hasItem(result(constraintMatcher, hasItem(hasValue(typeDescriptor(Activator.class)))))));
        store.commitTransaction();
    }

    /**
     * Retrieves the URL of the test MANIFEST.MF file.
     *
     * @return The URL.
     * @throws IOException If a problem occurs.
     */
    private URL getManifestUrl() throws IOException {
        return new File(OsgiBundleIT.class.getResource("/").getFile(), "/META-INF/MANIFEST.MF").toURI().toURL();
    }
}
