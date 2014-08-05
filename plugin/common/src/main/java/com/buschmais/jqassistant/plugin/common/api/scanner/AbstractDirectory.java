package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.io.File;

public abstract class AbstractDirectory {

    private File directory;

    public AbstractDirectory(File directory) {
        this.directory = directory;
    }

    public File getDirectory() {
        return directory;
    }
}
