package com.buschmais.jqassistant.plugin.javaee6.test.scanner;

import java.io.File;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

public class EarScannerPluginIT extends AbstractPluginIT {

    @Test
    public void earArchive() {
        File earFile = new File("target/test-data/javaee-inject-example-ear.ear");
        store.beginTransaction();
        getScanner().scan(earFile, earFile.getAbsolutePath(), null);
        store.commitTransaction();
    }

}
