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

@ScannerPlugin.Requires(FileDescriptor.class)
public class YMLFileScannerPlugin extends AbstractScannerPlugin<FileResource, YMLFileDescriptor> {
    private static final Logger LOGGER = LoggerFactory.getLogger(YMLFileScannerPlugin.class);

    public static final String PROPERTY_INCLUDE = "yaml.file.include";
    public static final String PROPERTY_EXCLUDE = "yaml.file.exclude";

    private FilePatternMatcher filePatternMatcher;

    protected FilePatternMatcher getFilePatternMatcher() {
        return filePatternMatcher;
    }

    protected boolean isFilePatternMatcherActive() {
        return null != getFilePatternMatcher();
    }

    /**
     * Supported file extensions for YAML file resources.
     */
    public final static String YAML_FILE_EXTENSION = ".yaml";
    public final static String YML_FILE_EXTENSION = ".yml";

    @Override
    protected void configure() {
        String inclusionPattern = getStringProperty(PROPERTY_INCLUDE, null);
        String exclusionPattern = getStringProperty(PROPERTY_EXCLUDE, null);

        configure(inclusionPattern, exclusionPattern);
    }

    // Enable unit testing
    protected void configure (final String inclusionPattern, final String exclusionPattern) {
        LOGGER.debug("YAML2: Including '{}' / Excluding '{}'", inclusionPattern, exclusionPattern);

        if (null != inclusionPattern || null != exclusionPattern) {
            filePatternMatcher = FilePatternMatcher.builder().include(inclusionPattern).exclude(exclusionPattern).build();
        }
    }

    @Override
    public boolean accepts(FileResource file, String path, Scope scope) {
        String lowercasePath = path.toLowerCase();
        boolean decision = true;

        if (isFilePatternMatcherActive()) {
            decision = getFilePatternMatcher().accepts(lowercasePath);
        } else {
            decision = lowercasePath.endsWith(YAML_FILE_EXTENSION) || lowercasePath.endsWith(YML_FILE_EXTENSION);
        }
        LOGGER.debug("YAML2: Checking '{}' ('{}') for acceptance: {}", path, lowercasePath, decision);

        return decision;
    }

    @Override
    public YMLFileDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        LoadSettings settings = LoadSettings.builder().build();
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
            documents.forEach(documentDescriptor -> yamlFileDescriptor.getDocuments().add(documentDescriptor));

            yamlFileDescriptor.setValid(true);
        } catch (GraphGenerationFailedException | YamlEngineException e) {
            LOGGER.warn("YAML file '{}' seems to be invalid and will be marked as invalid. Result graph might be incorrect.", path);
        }

        return yamlFileDescriptor;
    }


    private YMLFileDescriptor handleFileStart(FileDescriptor fileDescriptor) {
        YMLFileDescriptor yamlFileDescriptor = getScannerContext().getStore().addDescriptorType(fileDescriptor, YMLFileDescriptor.class, YMLFileDescriptor.class);
        return yamlFileDescriptor;
    }
}
