package com.buschmais.jqassistant.commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.commandline.plugin.ArtifactProviderFactory;
import com.buschmais.jqassistant.commandline.task.RegisteredTask;
import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationBuilder;
import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationMappingLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginClassLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginConfigurationReader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginResolver;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginRepositoryImpl;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginResolverImpl;
import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;
import com.buschmais.jqassistant.core.store.api.StoreFactory;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SysPropConfigSource;
import org.apache.commons.cli.*;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.buschmais.jqassistant.commandline.configuration.MavenSettingsConfigSourceBuilder.createMavenSettingsConfigSource;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * The main class, i.e. the entry point for the CLI.
 *
 * @author jn4, Kontext E GmbH, 23.01.14
 * @author Dirk Mahler
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String CMDLINE_OPTION_CONFIG_LOCATIONS = "-configurationLocations";

    private static final String CMDLINE_OPTION_MAVEN_SETTINGS = "-mavenSettings";

    /**
     * The main method.
     *
     * @param args
     *     The command line arguments.
     */
    public static void main(String[] args) {
        try {
            new Main().run(args);
        } catch (CliExecutionException e) {
            String message = getErrorMessage(e);
            LOGGER.error(message);
            System.exit(e.getExitCode());
        }
    }

    /**
     * Run tasks according to the given arguments.
     *
     * @param args
     *     The arguments.
     * @throws CliExecutionException
     *     If execution fails.
     */
    public void run(String[] args) throws CliExecutionException {
        Options options = gatherOptions();
        CommandLine commandLine = getCommandLine(args, options);
        interpretCommandLine(commandLine, options);
    }

    /**
     * Extract an error message from the given exception and its causes.
     *
     * @param e
     *     The exception.
     * @return The error message.
     */
    private static String getErrorMessage(CliExecutionException e) {
        StringBuilder messageBuilder = new StringBuilder();
        Throwable current = e;
        do {
            messageBuilder.append("-> ");
            messageBuilder.append(current.getMessage());
            current = current.getCause();
        } while (current != null);
        return messageBuilder.toString();
    }

    /**
     * Initialize the plugin repository.
     *
     * @param configuration
     *     The {@link CliConfiguration}
     * @param artifactProvider
     *     The {@link ArtifactProvider}
     * @return The repository.
     */
    private PluginRepository getPluginRepository(CliConfiguration configuration, ArtifactProvider artifactProvider) {
        PluginResolver pluginResolver = new PluginResolverImpl(artifactProvider);
        PluginClassLoader pluginClassLoader = pluginResolver.createClassLoader(Task.class.getClassLoader(), configuration);
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(pluginClassLoader);
        PluginRepositoryImpl pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
        pluginRepository.initialize();
        return pluginRepository;
    }

    /**
     * Gather all options which are supported by the task (i.e. including standard and specific options).
     *
     * @return The options.
     */
    private Options gatherOptions() {
        final Options options = new Options();
        gatherTasksOptions(options);
        gatherStandardOptions(options);
        return options;
    }

    /**
     * Gathers the standard options shared by all tasks.
     *
     * @param options
     *     The standard options.
     */
    private void gatherStandardOptions(final Options options) {
        options.addOption(Option.builder("C")
            .longOpt("configurationLocations")
            .desc("The list of configuration locations, i.e. YAML files and directories")
            .hasArgs()
            .valueSeparator(',')
            .build());
        options.addOption(Option.builder("M")
            .longOpt("mavenSettings")
            .desc("The location of a Maven settings.xml file to use for repository, proxy and mirror configurations.")
            .hasArg()
            .valueSeparator(',')
            .build());
        options.addOption(Option.builder("D")
            .desc("Additional configuration property.")
            .hasArgs()
            .valueSeparator('=')
            .build());
    }

    /**
     * Gathers the task specific options for all tasks.
     *
     * @param options
     *     The task specific options.
     */
    private void gatherTasksOptions(Options options) {
        for (Task task : RegisteredTask.getTasks()) {
            for (Option option : task.getOptions()) {
                options.addOption(option);
            }
        }
    }

    /**
     * Parse the command line and execute the requested task.
     *
     * @param commandLine
     *     The command line.
     * @param options
     *     The known options.
     * @throws CliExecutionException
     *     If an error occurs.
     */
    private void interpretCommandLine(CommandLine commandLine, Options options) throws CliExecutionException {
        File workingDirectory = new File(".");
        File userHome = new File(System.getProperty("user.home"));
        List<Task> tasks = getTasks(commandLine);
        CliConfiguration configuration = getCliConfiguration(commandLine, workingDirectory, userHome, tasks);
        if (configuration.skip()) {
            LOGGER.info("Skipping execution.");
        } else {
            ArtifactProviderFactory artifactProviderFactory = new ArtifactProviderFactory(userHome);
            ArtifactProvider artifactProvider = artifactProviderFactory.create(configuration);
            PluginRepository pluginRepository = getPluginRepository(configuration, artifactProvider);
            StoreFactory storeFactory = new StoreFactory(pluginRepository.getStorePluginRepository(), artifactProvider);
            ClassLoader contextClassLoader = currentThread().getContextClassLoader();
            currentThread().setContextClassLoader(pluginRepository.getClassLoader());
            try {
                executeTasks(tasks, configuration, options, pluginRepository, storeFactory);
            } finally {
                currentThread().setContextClassLoader(contextClassLoader);
            }
        }
    }

    private List<Task> getTasks(CommandLine commandLine) throws CliExecutionException {
        List<String> taskNames = commandLine.getArgList();
        if (taskNames.isEmpty()) {
            return singletonList(RegisteredTask.HELP.getTask());
        }
        List<Task> tasks = new ArrayList<>(taskNames.size());
        for (String taskName : taskNames) {
            tasks.add(RegisteredTask.fromName(taskName));
        }
        return tasks;
    }

    private CliConfiguration getCliConfiguration(CommandLine commandLine, File workingDirectory, File userHome, List<Task> tasks)
        throws CliConfigurationException {
        List<String> configLocations = getConfigLocations(commandLine);
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder("TaskConfigSource", 200);
        Map<String, String> properties = commandLine.getOptionProperties("D")
            .entrySet()
            .stream()
            .collect(toMap(entry -> String.valueOf(entry.getKey()), entry -> String.valueOf(entry.getValue())));
        PropertiesConfigSource commandLineProperties = new PropertiesConfigSource(properties, "Command line properties", 400);
        ConfigSource mavenSettingsConfigSource = createMavenSettingsConfigSource(userHome, getMavenSettings(commandLine));
        ConfigSource configSource = configurationBuilder.build();
        for (Task task : tasks) {
            task.configure(commandLine, configurationBuilder);
        }
        return ConfigurationMappingLoader.builder(CliConfiguration.class, configLocations)
            .withUserHome(userHome)
            .withWorkingDirectory(workingDirectory)
            .withClasspath()
            .withEnvVariables()
            .load(configSource, new SysPropConfigSource(), commandLineProperties, mavenSettingsConfigSource);
    }

    private Optional<File> getMavenSettings(CommandLine commandLine) {
        if (commandLine.hasOption(CMDLINE_OPTION_MAVEN_SETTINGS)) {
            return of(new File(commandLine.getOptionValue(CMDLINE_OPTION_MAVEN_SETTINGS)));
        }
        return empty();
    }

    private List<String> getConfigLocations(CommandLine commandLine) {
        if (commandLine.hasOption(CMDLINE_OPTION_CONFIG_LOCATIONS)) {
            return stream(commandLine.getOptionValues(CMDLINE_OPTION_CONFIG_LOCATIONS)).filter(configLocation -> !configLocation.isEmpty())
                .collect(toList());
        }
        return emptyList();
    }

    protected void executeTasks(List<Task> tasks, CliConfiguration configuration, Options options, PluginRepository pluginRepository, StoreFactory storeFactory)
        throws CliExecutionException {
        try {
            for (Task task : tasks) {
                executeTask(task, configuration, options, pluginRepository, storeFactory);
            }
        } finally {
            pluginRepository.destroy();
        }
    }

    /**
     * Parse the command line
     *
     * @param args
     *     The arguments.
     * @param options
     *     The known options.
     * @return The command line.
     */
    private CommandLine getCommandLine(String[] args, Options options) throws CliExecutionException {
        final CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            throw new CliExecutionException("Cannot parse command line arguments", e);
        }
    }

    /**
     * Executes a task.
     *
     * @param task
     *     The task.
     * @param configuration
     *     The {@link CliConfiguration}-
     * @param options
     *     The CLI options.
     * @param storeFactory
     *     The {@link StoreFactory}
     * @throws CliExecutionException
     *     If the execution fails.
     */
    private void executeTask(Task task, CliConfiguration configuration, Options options, PluginRepository pluginRepository, StoreFactory storeFactory)
        throws CliExecutionException {
        task.initialize(pluginRepository, storeFactory);
        task.run(configuration, options);
    }
}
