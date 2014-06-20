package com.buschmais.jqassistant.scm.cli;

import static com.buschmais.jqassistant.core.scanner.api.iterable.IterableConsumer.consume;
import static com.buschmais.jqassistant.plugin.java.api.JavaScope.CLASSPATH;
import static com.buschmais.jqassistant.scm.cli.Log.getLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.plugin.impl.ScannerPluginRepositoryImpl;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.impl.store.descriptor.ArtifactDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.api.ClassesDirectory;

/**
 * @author jn4, Kontext E GmbH, 23.01.14
 */
public class ClassToNeo4JImporter extends CommonJqAssistantTask implements OptionsConsumer {
    private final List<String> directoryNames = new ArrayList<>();

    public ClassToNeo4JImporter() {
        super("scan");
    }

    protected void doTheTask(final Store store) {
        store.reset();
        try {
            for (String directoryName : directoryNames) {
                properties = new HashMap<>();
                scanDirectory(store, directoryName, getScannerPluginRepository(store, properties).getScannerPlugins());
            }
        } catch (PluginRepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    protected ScannerPluginRepository getScannerPluginRepository(Store store, Map<String, Object> properties) {
        try {
            return new ScannerPluginRepositoryImpl(pluginConfigurationReader, store, properties);
        } catch (PluginRepositoryException e) {
            throw new RuntimeException("Cannot create rule plugin repository.", e);
        }
    }

    private void scanDirectory(Store store, final String directoryName, final List<ScannerPlugin<?>> scannerPlugins) {
        final File directory = new File(directoryName);
        if (!directory.exists()) {
            getLog().info("Directory '" + directory.getAbsolutePath() + "' does not exist, skipping scan.");
        } else {
            store.beginTransaction();
            try {
                final ArtifactDirectoryDescriptor artifactDescriptor = getOrCreateArtifactDescriptor(store);
                final Scanner scanner = new ScannerImpl(scannerPlugins);
                try {
                    consume(scanner.scan(new ClassesDirectory(directory, artifactDescriptor), CLASSPATH));
                } catch (IOException e) {
                    throw new RuntimeException("Cannot scan directory '" + directory.getAbsolutePath() + "'", e);
                }
            } finally {
                store.commitTransaction();
            }
        }
    }

    private ArtifactDirectoryDescriptor getOrCreateArtifactDescriptor(final Store store) {
        final String id = "dummy:id";
        ArtifactDirectoryDescriptor artifactDescriptor = store.find(ArtifactDirectoryDescriptor.class, id);
        if (artifactDescriptor == null) {
            artifactDescriptor = store.create(ArtifactDirectoryDescriptor.class, id);
            artifactDescriptor.setGroup("dummy");
            artifactDescriptor.setName("dummy");
            artifactDescriptor.setVersion("dummy");
            artifactDescriptor.setClassifier("dummy");
            artifactDescriptor.setType("dummy");
        }
        return artifactDescriptor;
    }

    @Override
    public void withOptions(final CommandLine options) {
        if (options.hasOption("d")) {
            for (String dir : options.getOptionValues("d")) {
                if (dir.trim().length() > 0)
                    directoryNames.add(dir);
            }
        }

        if (directoryNames.isEmpty()) {
            throw new MissingConfigurationParameterException("No directories to be scanned given, use 'dirs' argument to specify some");
        }
    }

    @SuppressWarnings("static-access")
    @Override
    protected void addFunctionSpecificOptions(final List<Option> options) {
        options.add(OptionBuilder.withArgName("d").withLongOpt("dirs").withDescription("directories to be scanned, comma separated").withValueSeparator(',')
                .hasArgs().create("d"));
    }
}
