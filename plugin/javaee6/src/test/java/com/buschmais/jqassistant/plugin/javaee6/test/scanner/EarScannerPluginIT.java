package com.buschmais.jqassistant.plugin.javaee6.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.PackageDescriptorMatcher.packageDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.model.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.matcher.PackageDescriptorMatcher;
import com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

/**
 * Verify scanning of EAR archives.
 */
public class EarScannerPluginIT extends AbstractPluginIT {

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
        List<Object> webXml = query("match (:Web:Application)-[:CONTAINS]->(web:Web:Xml) return web").getColumn("web");
        assertThat(webXml, hasSize(1));
        List<PackageDescriptor> packages = query("match (:Web:Application)-[:CONTAINS]->(package:Java:Package) return package").getColumn("package");
        assertThat(packages, hasSize(5));
        assertThat(packages, hasItems(
                packageDescriptor("org"),
                packageDescriptor("org.wicketstuff"),
                packageDescriptor("org.wicketstuff.javaee"),
                packageDescriptor("org.wicketstuff.javaee.example"),
                packageDescriptor("org.wicketstuff.javaee.example.pages")));
        List<TypeDescriptor> types = query("match (:Web:Application)-[:CONTAINS]->(type:Java:Type) return type").getColumn("type");
        assertThat(types, hasSize(6));
        assertThat(types, hasItems(typeDescriptor("org.wicketstuff.javaee.example.WicketJavaEEApplication")));
        store.commitTransaction();
    }

}
