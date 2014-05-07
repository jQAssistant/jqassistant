package com.buschmais.jqassistant.scm.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.pluginrepository.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.pluginrepository.impl.ScannerPluginRepositoryImpl;
import com.buschmais.jqassistant.core.scanner.api.FileScanner;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.FileScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.store.descriptor.ArtifactDescriptor;

import static com.buschmais.jqassistant.scm.cli.Log.getLog;


/**
 * @author jn4, Kontext E GmbH, 23.01.14
 */
public class ClassToNeo4JImporter extends CommonJqAssistantTask implements OptionsConsumer{
    private final List<String> directoryNames = new ArrayList<>();

    public ClassToNeo4JImporter() {
        super("scan");
    }

    protected void doTheTask(final Store store) {
        store.reset();
        try {
            for (String directoryName : directoryNames) {
                properties = new HashMap<>();
                scanDirectory(store, directoryName, getScannerPluginRepository(store, properties).getFileScannerPlugins());
            }
        } catch (PluginReaderException e) {
            throw new RuntimeException(e);
        }
    }

    protected ScannerPluginRepository getScannerPluginRepository(Store store, Map<String, Object> properties) {
        try {
            return new ScannerPluginRepositoryImpl(store, properties);
        } catch (PluginReaderException e) {
            throw new RuntimeException("Cannot create rule plugin repository.", e);
        }
    }

    private void scanDirectory(Store store, final String directoryName, final List<FileScannerPlugin> scannerPlugins) {
        final File directory = new File(directoryName);
        if (!directory.exists()) {
            getLog().info("Directory '" + directory.getAbsolutePath() + "' does not exist, skipping scan.");
        } else {
            store.beginTransaction();
            try {
                final ArtifactDescriptor artifactDescriptor = getOrCreateArtifactDescriptor(store);
                final FileScanner scanner = new FileScannerImpl(scannerPlugins);
                try {
                    for (FileDescriptor descriptor : scanner.scanDirectory(directory)) {
                        artifactDescriptor.addContains(descriptor);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Cannot scan directory '" + directory.getAbsolutePath() + "'", e);
                }
            } finally {
                store.commitTransaction();
            }
        }
    }

    private ArtifactDescriptor getOrCreateArtifactDescriptor(final Store store) {
        final String id = "dummy:id";
        ArtifactDescriptor artifactDescriptor = store.find(ArtifactDescriptor.class, id);
        if (artifactDescriptor == null) {
            artifactDescriptor = store.create(ArtifactDescriptor.class, id);
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
        if(options.hasOption("d")) {
            for (String dir : options.getOptionValues("d")) {
                if(dir.trim().length() > 0) directoryNames.add(dir);
            }
        }

        if(directoryNames.isEmpty()) {
            throw new MissingConfigurationParameterException("No directories to be scanned given, use 'dirs' argument to specify some");
        }
    }

    @Override
    protected void addFunctionSpecificOptions(final List<Option> options) {
        options.add(OptionBuilder
                            .withArgName("d")
                            .withLongOpt("dirs")
                            .withDescription("directories to be scanned, comma separated")
                            .withValueSeparator(',')
                            .hasArgs()
                            .create("d"));
    }
}
