package com.buschmais.jqassistant.commandline;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.commandline.plugin.PluginResolverFactory;
import com.buschmais.jqassistant.commandline.task.RegisteredTask;
import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationBuilder;
import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginClassLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginConfigurationReader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginResolver;
import com.buschmais.jqassistant.core.runtime.impl.configuration.ConfigurationLoaderImpl;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginRepositoryImpl;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SysPropConfigSource;
import org.apache.commons.cli.*;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.buschmais.jqassistant.commandline.configuration.MavenSettingsConfigSourceBuilder.createConfigSource;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * The main class, i.e. the entry point for the CLI.
 *
 * @author jn4, Kontext E GmbH, 23.01.14
 * @author Dirk Mahler
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String ENV_JQASSISTANT_HOME = "JQASSISTANT_HOME";
    private static final String DIRECTORY_PLUGINS = "plugins";

    private static final String CMDLINE_OPTION_CONFIG_LOCATIONS = "-configurationLocations";

    /**
     * The main method.
     *
     * @param args
     *     The command line arguments.
     * @throws IOException
     *     If an error occurs.
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
        StringBuffer messageBuilder = new StringBuffer();
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
     * @return The repository.
     * @throws CliExecutionException
     *     If initialization fails.
     */
    private PluginRepository getPluginRepository(CliConfiguration configuration, File userHome) throws CliExecutionException {
        // create classloader for the plugins/ directory.
        ClassLoader pluginDirectoryClassLoader = createPluginClassLoader();
        PluginResolverFactory pluginResolverFactory = new PluginResolverFactory(userHome);
        PluginResolver pluginResolver = pluginResolverFactory.create(configuration);
        // create plugin classloader using classloader for plugins/ directory as parent, adding plugins to be resolved from PluginResolver
        PluginClassLoader pluginClassLoader = pluginResolver.createClassLoader(pluginDirectoryClassLoader, configuration);
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(pluginClassLoader);
        return new PluginRepositoryImpl(pluginConfigurationReader);
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
    @SuppressWarnings("static-access")
    private void gatherStandardOptions(final Options options) {
        options.addOption(Option.builder("C")
            .longOpt("configurationLocations")
            .desc("The list of configuration locations, i.e. YAML files and directories")
            .hasArgs()
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
        List<String> configLocations = getConfigLocations(commandLine);
        ConfigurationLoader configurationLoader = new ConfigurationLoaderImpl(userHome, workingDirectory, configLocations);
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder("TaskConfigSource", 110);
        PropertiesConfigSource commandLineProperties = new PropertiesConfigSource(commandLine.getOptionProperties("D"), "Command line properties");
        ConfigSource mavenSettings = createConfigSource(userHome);
        CliConfiguration configuration = configurationLoader.load(CliConfiguration.class, configurationBuilder.build(), new SysPropConfigSource(),
            commandLineProperties, mavenSettings);
        if (configuration.skip()) {
            LOGGER.info("Skipping execution.");
        } else {
            List<String> taskNames = commandLine.getArgList();
            List<Task> tasks = getTasks(taskNames, commandLine, configurationBuilder);
            PluginRepository pluginRepository = getPluginRepository(configuration, userHome);
            executeTasks(tasks, configuration, pluginRepository, options);
        }
    }

    private List<Task> getTasks(List<String> taskNames, CommandLine commandLine, ConfigurationBuilder configurationBuilder) {
        if (taskNames.isEmpty()) {
            return singletonList(RegisteredTask.HELP.getTask());
        }
        List<Task> tasks = new ArrayList<>(taskNames.size());
        for (String taskName : taskNames) {
            try {
                Task task = RegisteredTask.fromName(taskName);
                task.configure(commandLine, configurationBuilder);
                tasks.add(task);
            } catch (CliExecutionException e) {
                printUsage(e.getMessage());
                System.exit(1);
            }
        }
        return tasks;
    }

    private List<String> getConfigLocations(CommandLine commandLine) {
        if (commandLine.hasOption(CMDLINE_OPTION_CONFIG_LOCATIONS)) {
            return stream(commandLine.getOptionValues(CMDLINE_OPTION_CONFIG_LOCATIONS)).filter(configLocation -> !configLocation.isEmpty())
                .collect(toList());
        }
        return emptyList();
    }

    protected void executeTasks(List<Task> tasks, CliConfiguration configuration, PluginRepository pluginRepository, Options options)
        throws CliExecutionException {
        try {
            pluginRepository.initialize();
            for (Task task : tasks) {
                executeTask(task, configuration, pluginRepository, options);
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
    private CommandLine getCommandLine(String[] args, Options options) {
        final CommandLineParser parser = new BasicParser();
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            printUsage(e.getMessage());
            System.exit(1);
        }
        return commandLine;
    }

    /**
     * Executes a task.
     *
     * @param task
     *     The task.
     * @param configuration
     *     The {@link CliConfiguration}-
     * @param options
     * @throws IOException
     */
    private void executeTask(Task task, CliConfiguration configuration, PluginRepository pluginRepository, Options options) throws CliExecutionException {
        task.initialize(pluginRepository);
        task.run(configuration, options);
    }

    /**
     * Print usage information.
     *
     * @param errorMessage
     *     The error message to append.
     */
    private void printUsage(final String errorMessage) {
        if (errorMessage != null) {
            System.out.println("Error: " + errorMessage);
        }
    }

    /**
     * Determine the JQASSISTANT_HOME directory.
     *
     * @return The directory or `null`.
     */
    private File getHomeDirectory() {
        String dirName = System.getenv(ENV_JQASSISTANT_HOME);
        if (dirName != null) {
            File dir = new File(dirName);
            if (dir.exists()) {
                LOGGER.debug("Using JQASSISTANT_HOME '" + dir.getAbsolutePath() + "'.");
                return dir;
            } else {
                LOGGER.warn("JQASSISTANT_HOME '" + dir.getAbsolutePath() + "' points to a non-existing directory.");
                return null;
            }
        }
        LOGGER.warn("JQASSISTANT_HOME is not set.");
        return null;
    }

    /**
     * Create the class loader to be used for detecting and loading plugins.
     *
     * @return The plugin class loader.
     * @throws com.buschmais.jqassistant.commandline.CliExecutionException
     *     If the plugins cannot be loaded.
     */
    private ClassLoader createPluginClassLoader() throws CliExecutionException {
        ClassLoader parentClassLoader = Task.class.getClassLoader();
        File homeDirectory = getHomeDirectory();
        if (homeDirectory != null) {
            File pluginDirectory = new File(homeDirectory, DIRECTORY_PLUGINS);
            if (pluginDirectory.exists()) {
                final List<URL> urls = new ArrayList<>();
                final Path pluginDirectoryPath = pluginDirectory.toPath();
                SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.toFile()
                            .getName()
                            .endsWith(".jar")) {
                            urls.add(file.toFile()
                                .toURI()
                                .toURL());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                };
                try {
                    Files.walkFileTree(pluginDirectoryPath, visitor);
                } catch (IOException e) {
                    throw new CliExecutionException("Cannot read plugin directory.", e);
                }
                LOGGER.debug("Using plugin URLs: " + urls);
                return new PluginClassLoader(parentClassLoader, urls);
            }
        }
        return parentClassLoader;
    }
}
