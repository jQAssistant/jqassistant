package com.buschmais.jqassistant.plugin.javaee6.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

/**
 *
 */
@Requires(FileDescriptor.class)
public class WarClassesFileScannerPlugin extends AbstractWarClassesResourceScannerPlugin<FileResource, FileDescriptor> {

}
