package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.stream.StreamSupport;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FilePatternMatcher;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDocumentDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLFileDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph.GraphGenerationFailedException;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph.GraphGenerator;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.EventParser;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.StreamNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Parse;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

@Requires(FileDescriptor.class)
public class YMLFileScannerPlugin extends AbstractScannerPlugin<FileResource, YMLFileDescriptor> {
    private static final Logger LOGGER = LoggerFactory.getLogger(YMLFileScannerPlugin.class);

    public static final String PROPERTY_INCLUDE = "yaml.file.include";
    public static final String PROPERTY_EXCLUDE = "yaml.file.exclude";

    private FilePatternMatcher filePatternMatcher;

    @Override
    protected void configure() {
        String inclusionPattern = getStringProperty(PROPERTY_INCLUDE, "*.yml, *.yaml");
        String exclusionPattern = getStringProperty(PROPERTY_EXCLUDE, null);
        LOGGER.debug("YAML2: Including '{}' / Excluding '{}'", inclusionPattern, exclusionPattern);
        filePatternMatcher = FilePatternMatcher.builder()
            .include(inclusionPattern)
            .exclude(exclusionPattern)
            .build();
    }

    // Enable unit testing

    @Override
    public boolean accepts(FileResource file, String path, Scope scope) {
        return filePatternMatcher.accepts(path.toLowerCase());
    }

    @Override
    public YMLFileDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        LoadSettings settings = LoadSettings.builder()
            .build();
        FileDescriptor fileDescriptor = context.getCurrentDescriptor();
        EventParser eventParser = new EventParser();
        YMLFileDescriptor yamlFileDescriptor = handleFileStart(fileDescriptor);
        yamlFileDescriptor.setValid(false);

        try (InputStream in = item.createStream()) {
            Parse parser = new Parse(settings);
            Iterable<Event> events = parser.parseInputStream(in);
            StreamNode streamNode = eventParser.parse(StreamSupport.stream(events.spliterator(), false));
            Store store = getScannerContext().getStore();
            GraphGenerator generator = new GraphGenerator(store);

            Collection<YMLDocumentDescriptor> documents = generator.generate(streamNode);
            documents.forEach(documentDescriptor -> yamlFileDescriptor.getDocuments()
                .add(documentDescriptor));

            yamlFileDescriptor.setValid(true);
        } catch (GraphGenerationFailedException | YamlEngineException e) {
            LOGGER.warn("YAML file '{}' seems to be invalid and will be marked as invalid. Result graph might be incorrect.", path);
        }

        return yamlFileDescriptor;
    }

    private YMLFileDescriptor handleFileStart(FileDescriptor fileDescriptor) {
        return getScannerContext().getStore()
            .addDescriptorType(fileDescriptor, YMLFileDescriptor.class, YMLFileDescriptor.class);
    }
}
