package com.buschmais.jqassistant.core.scanner.impl;

import java.io.IOException;
import java.util.List;

import com.buschmais.jqassistant.core.scanner.api.FileScanner;
import com.buschmais.jqassistant.core.scanner.api.ProjectScanner;
import com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin;

/**
 * Implementation of the {@link ProjectScanner}.
 */
public class ProjectScannerImpl implements ProjectScanner {

    private final FileScanner fileScanner;
    private final List<ProjectScannerPlugin> projectScannerPlugins;

    public ProjectScannerImpl(FileScanner fileScanner, List<ProjectScannerPlugin> projectScannerPlugins) {
        this.fileScanner = fileScanner;
        this.projectScannerPlugins = projectScannerPlugins;
    }

    @Override
    public void scan() throws IOException {
        for (ProjectScannerPlugin scannerPlugin : projectScannerPlugins) {
            scannerPlugin.scan(fileScanner);
        }
    }

}
