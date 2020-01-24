package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
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

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@ScannerPlugin.Requires(FileDescriptor.class)
public class YMLFileScannerPlugin extends AbstractScannerPlugin<FileResource, YMLFileDescriptor> {

    /**
     * Supported file extensions for YAML file resources.
     */
    public final static String YAML_FILE_EXTENSION = ".yaml";
    public final static String YML_FILE_EXTENSION = ".yml";

    public final static String ACTIVATION_PROPERTY = "jqassistant.yaml2.activate";


    private ParsingContext context = new ParsingContext();

    @Override
    public boolean accepts(FileResource file, String path, Scope scope) {
        boolean activated = isPluginActive();
        String lowercasePath = path.toLowerCase();
        return activated && lowercasePath.endsWith(YAML_FILE_EXTENSION) || lowercasePath.endsWith(YML_FILE_EXTENSION);
    }

    @Override
    public YMLFileDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        LoadSettings settings = LoadSettings.builder().build();
        FileDescriptor fileDescriptor = context.getCurrentDescriptor();
        EventParser eventParser = new EventParser();

        // todo implement handleFileEnd
        // todo take it from the parsing context
        YMLFileDescriptor yamlFileDescriptor = handleFileStart(fileDescriptor);


        try (InputStream in = item.createStream()) {
            Parse parser = new Parse(settings);
            Iterable<Event> events = parser.parseInputStream(in);
            StreamNode streamNode = eventParser.parse(StreamSupport.stream(events.spliterator(), false));
            Store store = getScannerContext().getStore();
            GraphGenerator generator = new GraphGenerator(store);
            Collection<YMLDocumentDescriptor> documents = generator.generate(streamNode);

            documents.forEach(documentDescriptor -> {
                yamlFileDescriptor.getDocuments().add(documentDescriptor);
            });
        } catch (RuntimeException re) {
            // todo Improve the errorhandling
            throw re;
        }

        return yamlFileDescriptor;
    }


    private YMLFileDescriptor handleFileStart(FileDescriptor fileDescriptor) {
        YMLFileDescriptor yamlFileDescriptor = getScannerContext().getStore().addDescriptorType(fileDescriptor, YMLFileDescriptor.class);
        ContextType<YMLFileDescriptor> inFile = ContextType.ofInFile(yamlFileDescriptor);
        context.enter(inFile);
        return yamlFileDescriptor;
    }

    private boolean isPluginActive() {
        Optional<String> setting = of(ofNullable(System.getProperty(ACTIVATION_PROPERTY)).orElse("false"));
        boolean activated = setting.map(s -> "true".equalsIgnoreCase(s)).get();

        return activated;
    }
}
