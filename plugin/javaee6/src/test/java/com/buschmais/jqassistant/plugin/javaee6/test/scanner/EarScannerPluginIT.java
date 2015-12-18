package com.buschmais.jqassistant.plugin.javaee6.test.scanner;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

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
        store.commitTransaction();
    }

}
