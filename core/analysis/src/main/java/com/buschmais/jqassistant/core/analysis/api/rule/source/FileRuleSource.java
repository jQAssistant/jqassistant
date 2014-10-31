package com.buschmais.jqassistant.core.analysis.api.rule.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A rule source which is provided from a file.
 */
public class FileRuleSource extends RuleSource {

    private File file;

    public FileRuleSource(File file) {
        this.file = file;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }
}
