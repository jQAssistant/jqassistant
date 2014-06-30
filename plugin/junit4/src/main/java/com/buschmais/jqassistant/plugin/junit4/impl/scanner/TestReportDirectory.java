package com.buschmais.jqassistant.plugin.junit4.impl.scanner;

import java.io.File;

import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractDirectory;

public class TestReportDirectory extends AbstractDirectory {

    public TestReportDirectory(File directory) {
        super(directory, null);
    }
}
