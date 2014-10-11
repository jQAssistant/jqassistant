package com.buschmais.jqassistant.examples.plugins.scanner;

import java.io.IOException;
import java.io.InputStream;

import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVReadProc;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.jqassistant.examples.plugins.scanner.model.CSVColumnDescriptor;
import com.buschmais.jqassistant.examples.plugins.scanner.model.CSVFileDescriptor;
import com.buschmais.jqassistant.examples.plugins.scanner.model.CSVRowDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.VirtualFile;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;

/**
 * A CSV file scanner plugin.
 */
public class CSVFileScannerPlugin extends AbstractScannerPlugin<VirtualFile> {

    @Override
    protected void initialize() {
    }

    @Override
    public Class<? super VirtualFile> getType() {
        return VirtualFile.class;
    }

    @Override
    public boolean accepts(VirtualFile item, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith(".csv");
    }

    @Override
    public FileDescriptor scan(VirtualFile item, String path, Scope scope, Scanner scanner) throws IOException {
        // Open the input stream for reading the file.
        try (InputStream stream = item.createStream()) {
            // Create the node for a CSV file.
            final Store store = scanner.getContext().getStore();
            final CSVFileDescriptor fileDescriptor = store.create(CSVFileDescriptor.class);
            // Parse the stream using OpenCSV.
            CSV csv = CSV.create();
            csv.read(stream, new CSVReadProc() {

                @Override
                public void procRow(int rowIndex, String... values) {
                    // Create the node for a row
                    CSVRowDescriptor rowDescriptor = store.create(CSVRowDescriptor.class);
                    fileDescriptor.getRows().add(rowDescriptor);
                    rowDescriptor.setLineNumber(rowIndex);
                    for (int i = 0; i < values.length; i++) {
                        // Create the node for a column
                        CSVColumnDescriptor columnDescriptor = store.create(CSVColumnDescriptor.class);
                        rowDescriptor.getColumns().add(columnDescriptor);
                        columnDescriptor.setIndex(i);
                        columnDescriptor.setValue(values[i]);
                    }
                }

            });
            return fileDescriptor;
        }
    }
}
