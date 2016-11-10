package com.buschmais.jqassistant.plugin.javaee6.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.PackageDescriptorMatcher.packageDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.api.model.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

/**
 * Verify scanning of EAR archives.
 */
public class EarScannerPluginIT extends AbstractPluginIT {

    @Test
    public void warArchive() {
        File warFile = new File("target/test-data/javaee-inject-example-war.war");
        store.beginTransaction();
        getScanner().scan(warFile, warFile.getAbsolutePath(), null);
        verifyWarArchive();
        store.commitTransaction();
    }

    @Test
    public void earArchive() {
        File earFile = new File("target/test-data/javaee-inject-example-ear.ear");
        store.beginTransaction();
        getScanner().scan(earFile, earFile.getAbsolutePath(), null);
        List<Object> earDescriptors = query("match (ear:Enterprise:Application:Zip:Archive:Container) return ear").getColumn("ear");
        assertThat(earDescriptors, hasSize(1));
        List<Object> applicationXml = query("match (:Enterprise:Application)-[:CONTAINS]->(application:Application:Xml) return application")
                .getColumn("application");
        assertThat(applicationXml, hasSize(1));
        List<Object> warDescriptors = query("match (:Enterprise:Application)-[:CONTAINS]->(war:Web:Application:Zip:Archive:Container) return war")
                .getColumn("war");
        assertThat(warDescriptors, hasSize(1));
        verifyWarArchive();
        store.commitTransaction();
    }

    private void verifyWarArchive() {
        List<Object> webXml = query("match (:Web:Application)-[:CONTAINS]->(web:Web:Xml) return web").getColumn("web");
        assertThat(webXml, hasSize(1));
        List<PackageDescriptor> packages = query("match (:Web:Application)-[:CONTAINS]->(package:Java:Package) return package").getColumn("package");
        assertThat(packages, hasSize(5));
        assertThat(packages, hasItems(packageDescriptor("org"), packageDescriptor("org.wicketstuff"), packageDescriptor("org.wicketstuff.javaee"),
                packageDescriptor("org.wicketstuff.javaee.example"), packageDescriptor("org.wicketstuff.javaee.example.pages")));
        List<TypeDescriptor> types = query("match (:Web:Application)-[:CONTAINS]->(type:Java:Type) return type").getColumn("type");
        assertThat(types, hasSize(6));
        assertThat(types, hasItems(typeDescriptor("org.wicketstuff.javaee.example.WicketJavaEEApplication")));
        // Verify that any type that is contained in an artifact is not required
        // at the same time
        TestResult duplicates = query("MATCH (a:Artifact),(a)-[:CONTAINS]->(t1:Type),(a)-[:REQUIRES]->(t2:Type) WHERE t1.fqn=t2.fqn return t1.fqn");
        assertThat(duplicates.getRows().size(), equalTo(0));
    }


}
