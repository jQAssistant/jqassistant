package com.buschmais.jqassistant.plugin.json.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.json.api.model.JSONFileDescriptor;
import com.buschmais.jqassistant.plugin.json.impl.parser.JSONLexer;
import com.buschmais.jqassistant.plugin.json.impl.parser.JSONParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;


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
        jsonFileDescriptor.setValid(false);

        try {
            JSONLexer lexer = new JSONLexer(new ANTLRInputStream(item.createStream()));
            JSONParser parser = new JSONParser(new CommonTokenStream(lexer));

            lexer.addErrorListener(new MyErrorListener(absolutePath));
            parser.addErrorListener(new MyErrorListener(absolutePath));

            parser.addParseListener(new JSONNestingListener());

            JSONParser.DocumentContext jsonDocumentContext = parser.document();

            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(new JSONTreeWalker(jsonFileDescriptor, scanner), jsonDocumentContext);

            // In case the content of the file is not parseable set valid=false
            // to help the user to identify non-parseable files
            jsonFileDescriptor.setValid(true);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse the file " + item.getFile().getAbsolutePath() + ".", e);
        }

        return jsonFileDescriptor;
    }

    private static class MyErrorListener extends BaseErrorListener {
        private final String absolutePath;

        public MyErrorListener(String absolutePath) {
            this.absolutePath = absolutePath;
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int l, int c, String msg, RecognitionException e) {
            throw new IllegalStateException("Failed to parse " + absolutePath + " at line " + l + " due to " + msg, e);
        }
    }
}
