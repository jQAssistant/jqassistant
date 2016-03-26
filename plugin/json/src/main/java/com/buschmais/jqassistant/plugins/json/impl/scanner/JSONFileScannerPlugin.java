package com.buschmais.jqassistant.plugins.json.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugins.json.api.model.JSONFileDescriptor;
import com.buschmais.jqassistant.plugins.json.impl.parser.JSONLexer;
import com.buschmais.jqassistant.plugins.json.impl.parser.JSONParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;


import java.io.IOException;

@ScannerPlugin.Requires(FileDescriptor.class)
public class JSONFileScannerPlugin extends AbstractScannerPlugin<FileResource, JSONFileDescriptor> {

    /**
     * Supported file extension for JSON file resources.
     */
    public final static String JSON_FILE_EXTENSION = ".json";

    @Override
    public boolean accepts(FileResource file, String path, Scope scope) throws IOException {
        return file.getFile().getName().toLowerCase().endsWith(JSON_FILE_EXTENSION);
    }

    @Override
    public JSONFileDescriptor scan(final FileResource item, String path, Scope scope, Scanner scanner)
         throws IOException {

        ScannerContext context = scanner.getContext();
        Store store = context.getStore();
        final String absolutePath = item.getFile().getAbsolutePath();

        FileDescriptor fileDescriptor = context.peek(FileDescriptor.class);
        JSONFileDescriptor jsonFileDescriptor = store.addDescriptorType(fileDescriptor, JSONFileDescriptor.class);

        jsonFileDescriptor.setFileName(absolutePath);
        jsonFileDescriptor.setParsed(false);

        try {
            JSONLexer lexer = new JSONLexer(new ANTLRInputStream(item.createStream()));
            JSONParser parser = new JSONParser(new CommonTokenStream(lexer));
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int l, int c, String msg, RecognitionException e) {
                    throw new IllegalStateException("Failed to parse " + absolutePath + " at line " + l + " due to " + msg, e);
                }
            });

            parser.addParseListener(new JSONParseListener(jsonFileDescriptor, scanner));
            parser.jsonDocument();

            // In case the content of the file is not parseable set parsed=false
            // to help the user to identify nonparseable files
            jsonFileDescriptor.setParsed(true);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse the file " + item.getFile().getAbsolutePath() + ".", e);
        }

        return jsonFileDescriptor;
    }
}
