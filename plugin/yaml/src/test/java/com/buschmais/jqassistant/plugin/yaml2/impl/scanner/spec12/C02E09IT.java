package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import org.junit.jupiter.api.Test;

class C02E09IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/spec-examples/example-c2-e09-single-document-with-two-comments.yaml";

    @Override
    protected String getSourceYAMLFile() {
        return YAML_FILE;
    }

    /* Note on the implemented tests
     * For this example it is enough to be able to read the document. Further
     * test only duplicate the already tested functionality.
     * Oliver Fischer // 2019-12-22
     */
    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }

}
