package com.buschmais.jqassistant.plugin.json.impl.scanner;

import java.io.IOException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ScannerPlugin.Requires(FileDescriptor.class)
public class JSONFileScannerPlugin extends AbstractScannerPlugin<FileResource, JSONFileDescriptor> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSONFileScannerPlugin.class);

    /**
     * Supported file extension for JSON file resources.
     */
    public final static String JSON_FILE_EXTENSION = ".json";

    @Override
    public boolean accepts(FileResource file, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith(JSON_FILE_EXTENSION);
    }

    @Override
    public JSONFileDescriptor scan(final FileResource item, String path, Scope scope, Scanner scanner)
         throws IOException {

        ScannerContext context = scanner.getContext();
        Store store = context.getStore();

        FileDescriptor fileDescriptor = context.getCurrentDescriptor();
        JSONFileDescriptor jsonFileDescriptor = store.addDescriptorType(fileDescriptor, JSONFileDescriptor.class);

        jsonFileDescriptor.setValid(false);

        try {
            JSONLexer lexer = new JSONLexer(CharStreams.fromStream(item.createStream()));
            JSONParser parser = new JSONParser(new CommonTokenStream(lexer));

            lexer.addErrorListener(new MyErrorListener(path));
            parser.addErrorListener(new MyErrorListener(path));

            parser.addParseListener(new JSONNestingListener());

            JSONParser.DocumentContext jsonDocumentContext = parser.document();

            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(new JSONTreeWalker(jsonFileDescriptor, scanner), jsonDocumentContext);

            // In case the content of the file is not parseable set valid=false
            // to help the user to identify non-parseable files
            jsonFileDescriptor.setValid(true);
        } catch (RecognitionException | IllegalStateException e) {
            LOGGER.warn("JSON file '{}' seems not to be valid, skipping.", path);
        }
        catch (NullPointerException e) {
            // todo Get rid of this strange error handling
            /*
             * This error handling is a kind of abuse of a stack trace, but
             * let me explain why I wrote such code.
             *
             * As of 2017-03-29 we use ANTLR 4.6 to parse JSON files.
             * In some cases where the JSON file is not valid ANTLR
             * will throw an RecognitionException and tries determine
             * the line number there the error occured in the JSON file.
             * ANTLR tries to get the line number from the current token
             * but this token might not exist and an NPE is thrown by
             * the JVM.
             *
             * See https://github.com/antlr/antlr4/issues/746 for the
             * corresponding bug report for ANTLR.
             *
             * Oliver B. Fischer, 2017-03-29
             */
            boolean stacktraceAvailable = e.getStackTrace().length > 0;
            if (stacktraceAvailable) {
                StackTraceElement stackTraceElement = e.getStackTrace()[0];
                boolean correctClass = "org.antlr.v4.runtime.Parser".equals(stackTraceElement.getClassName());
                boolean correctMethod = "notifyErrorListeners".equals(stackTraceElement.getMethodName());

                // Suppress NPE caused by a bug in ANTLR
                if (!correctClass && !correctMethod) {
                    throw e;
                }
            } else {
                throw e;
            }
        }

        return jsonFileDescriptor;
    }

    private static class MyErrorListener extends BaseErrorListener {
        private final String absolutePath;

        public MyErrorListener(String absolutePath) {
            this.absolutePath = absolutePath;
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine,
                                String msg, RecognitionException e) {
            LOGGER.warn("Failed to parse '{}' at {}:{}, due to '{}'.",
                        absolutePath, line, charPositionInLine, msg);

            throw e;
        }
    }
}
