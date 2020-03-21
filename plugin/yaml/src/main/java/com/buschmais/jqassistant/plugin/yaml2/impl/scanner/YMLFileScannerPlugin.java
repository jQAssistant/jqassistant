package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.stream.StreamSupport;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDocumentDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLFileDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.EventParser;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.StreamNode;

import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Parse;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

@ScannerPlugin.Requires(FileDescriptor.class)
public class YMLFileScannerPlugin extends AbstractScannerPlugin<FileResource, YMLFileDescriptor> {

    /**
     * Supported file extensions for YAML file resources.
     */
    public final static String YAML_FILE_EXTENSION = ".yaml";
    public final static String YML_FILE_EXTENSION = ".yml";

    private ParsingContext context = new ParsingContext();

    @Override
    public boolean accepts(FileResource file, String path, Scope scope) {
        String lowercasePath = path.toLowerCase();
        return lowercasePath.endsWith(YAML_FILE_EXTENSION) || lowercasePath.endsWith(YML_FILE_EXTENSION);
    }

    @Override
    public YMLFileDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        LoadSettings settings = LoadSettings.builder().build();
        FileDescriptor fileDescriptor = context.getCurrentDescriptor();
        EventParser eventParser = new EventParser();
        YMLFileDescriptor yamlFileDescriptor = handleFileStart(fileDescriptor);

        try (InputStream in = item.createStream()) {
            Parse parser = new Parse(settings);
            Iterable<Event> events = parser.parseInputStream(in);
            yamlFileDescriptor.setValid(true);
            StreamNode streamNode = eventParser.parse(StreamSupport.stream(events.spliterator(), false));
            Store store = getScannerContext().getStore();
            GraphGenerator generator = new GraphGenerator(store);
            Collection<YMLDocumentDescriptor> documents = generator.generate(streamNode);

            documents.forEach(documentDescriptor -> {
                yamlFileDescriptor.getDocuments().add(documentDescriptor);
            });
        } catch (GraphGenerationFailedException | YamlEngineException e) {
            yamlFileDescriptor.setValid(false);
        }

        return yamlFileDescriptor;
    }


    private YMLFileDescriptor handleFileStart(FileDescriptor fileDescriptor) {
        YMLFileDescriptor yamlFileDescriptor = getScannerContext().getStore().addDescriptorType(fileDescriptor, YMLFileDescriptor.class);
        ContextType<YMLFileDescriptor> inFile = ContextType.ofInFile(yamlFileDescriptor);
        context.enter(inFile);
        return yamlFileDescriptor;
    }
}
