package com.buschmais.jqassistant.scm.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.cli.Option;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.pluginrepository.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.pluginrepository.impl.ScannerPluginRepositoryImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;

import static com.buschmais.jqassistant.scm.cli.Log.getLog;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public abstract class CommonJqAssistantTask implements JqAssistantTask {
    protected final String taskName;
    protected Map<String,Object> properties;

    protected CommonJqAssistantTask(final String taskName) {
        this.taskName = taskName;
    }

    @Override
    public void initialize(Map<String,Object> properties) {
        this.properties = properties;
    }

    @Override
    public String getName() {
        return taskName;
    }

    protected Store getStore() {
        File directory = new File("./tmp/store"); // TODO take directory from
                                                  // properties
        getLog().info("Opening store in directory '" + directory.getAbsolutePath() + "'");
        directory.getParentFile().mkdirs();
        return new EmbeddedGraphStore(directory.getAbsolutePath());
    }

    @Override
    public void run() {
        List<Class<?>> descriptorTypes;
        final Store store = getStore();

        try {
            descriptorTypes = getScannerPluginRepository(store, properties).getDescriptorTypes();
        } catch (PluginReaderException e) {
            throw new RuntimeException("Cannot get descriptor mappers.", e);
        }

        try {
            store.start(descriptorTypes);
            doTheTask(store);
        } finally {
            store.stop();
        }
    }

    protected ScannerPluginRepository getScannerPluginRepository(Store store, Map<String, Object> properties) {
        try {
            return new ScannerPluginRepositoryImpl(store, properties);
        } catch (PluginReaderException e) {
            throw new RuntimeException("Cannot create rule plugin repository.", e);
        }
    }

    @Override
    public List<Option> getOptions() {
        final List<Option> options = new ArrayList<>();
        addFunctionSpecificOptions(options);
        return options;
    }

    protected void addFunctionSpecificOptions(final List<Option> options) {
    }

    protected abstract void doTheTask(final Store store);
}
