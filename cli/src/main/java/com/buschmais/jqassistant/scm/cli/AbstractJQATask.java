package com.buschmais.jqassistant.scm.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import com.buschmais.jqassistant.core.analysis.api.Console;
import com.buschmais.jqassistant.core.plugin.api.*;
import com.buschmais.jqassistant.core.plugin.impl.ModelPluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.ReportPluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.RulePluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.ScannerPluginRepositoryImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.common.report.ReportHelper;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public abstract class AbstractJQATask implements JQATask {

    protected static final String CMDLINE_OPTION_S = "s";
    protected static final String CMDLINE_OPTION_REPORTDIR = "reportDirectory";

    protected static final String XML_REPORT_FILE = "jqassistant-report.xml";

    private static final Console LOG = Log.getLog();

    protected Map<String, Object> properties;
    // protected PluginConfigurationReader pluginConfigurationReader;
    protected String storeDirectory;
    protected ReportHelper reportHelper;
    protected ModelPluginRepository modelPluginRepository;
    protected ScannerPluginRepository scannerPluginRepository;
    protected RulePluginRepository rulePluginRepository;
    protected ReportPluginRepository reportPluginRepository;

    /**
     * Constructor.
     *
     * @param pluginConfigurationReader
     *            The
     *            {@link com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader}
     */
    protected AbstractJQATask(PluginConfigurationReader pluginConfigurationReader) {
        try {
            modelPluginRepository = new ModelPluginRepositoryImpl(pluginConfigurationReader);
            scannerPluginRepository = new ScannerPluginRepositoryImpl(pluginConfigurationReader, properties);
            rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
            reportPluginRepository = new ReportPluginRepositoryImpl(pluginConfigurationReader, properties);
        } catch (PluginRepositoryException e) {
            throw new RuntimeException("Cannpt create plugin repositories.", e);
        }
        this.reportHelper = new ReportHelper(Log.getLog());
    }

    @Override
    public void initialize(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public void run() {
        List<Class<?>> descriptorTypes;
        final Store store = getStore();

        try {
            descriptorTypes = modelPluginRepository.getDescriptorTypes();
        } catch (PluginRepositoryException e) {
            throw new RuntimeException("Cannot get model.", e);
        }
        try {
            store.start(descriptorTypes);
            executeTask(store);
        } finally {
            store.stop();
        }
    }

    @Override
    public void withStandardOptions(CommandLine options) {
        storeDirectory = getOptionValue(options, CMDLINE_OPTION_S, DEFAULT_STORE_DIRECTORY);
    }

    @Override
    public List<Option> getOptions() {
        final List<Option> options = new ArrayList<>();
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_S).withLongOpt("storeDirectory").withDescription("The location of the Neo4j database.").hasArgs()
                .create(CMDLINE_OPTION_S));
        addTaskOptions(options);
        return options;
    }

    protected List<String> getOptionValues(CommandLine options, String option, List<String> defaultValues) {
        if (options.hasOption(option)) {
            List<String> names = new ArrayList<>();
            for (String elementName : options.getOptionValues(option)) {
                if (elementName.trim().length() > 0)
                    names.add(elementName);
            }
            return names;
        }
        return defaultValues;
    }

    protected String getOptionValue(CommandLine options, String option, String defaultValue) {
        if (options.hasOption(option)) {
            return options.getOptionValue(option);
        } else {
            return defaultValue;
        }
    }

    protected void addTaskOptions(final List<Option> options) {
    }

    /**
     * Return the {@link Store} instance.
     *
     * @return The store.
     */
    protected Store getStore() {
        File directory = new File(storeDirectory);
        LOG.info("Opening store in directory '" + directory.getAbsolutePath() + "'");
        directory.getParentFile().mkdirs();
        return new EmbeddedGraphStore(directory.getAbsolutePath());
    }

    protected abstract void executeTask(final Store store);
}
