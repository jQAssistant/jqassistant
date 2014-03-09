package com.buschmais.jqassistant.core.pluginmanager.api;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;

import javax.xml.transform.Source;
import java.util.List;
import java.util.Properties;

/**
 * Defines the interface for the scanner plugin repository.
 */
public interface RulePluginRepository extends PluginRepository {

    /**
     * Get a list of sources providing rules.
     *
     * @return The list of sources providing rules.
     */
    List<Source> getRuleSources();

}
