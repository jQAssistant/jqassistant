package com.buschmais.jqassistant.scm.cli;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author jn4, Kontext E GmbH, 23.01.14
 * @author Dirk Mahler
 */
public class Main {

    private static final Map<String, JQATask> tasks = new HashMap<>();

    public static void main(String[] args) throws IOException {
        initializeLogging();

        putTasksIntoMap(asList(new ScanTask(), new ServerTask(), new AnalyzeTask(), new ResetTask()));

        try {
            interpretCommandLine(args);
        } catch (JqaConstraintViolationException e) {
            System.out.println(e.getMessage());
            System.exit(2);
        }
    }

    private static void initializeLogging() {
    }

    private static Options gatherOptions() {
        final Options options = new Options();
        gatherTasksOptions(options);
        gatherStandardOptions(options);
        return options;
    }

    @SuppressWarnings("static-access")
    private static void gatherStandardOptions(final Options options) {
        options.addOption(OptionBuilder.withArgName("p").withDescription("Path to property file; default is jqassistant.properties in the class path")
                .withLongOpt("properties").hasArg().create("p"));
        options.addOption(new Option("help", "print this message"));
    }

    private static void gatherTasksOptions(final Options options) {
        for (OptionsProvider optionsProvider : tasks.values()) {
            for (Option option : optionsProvider.getOptions()) {
                options.addOption(option);
            }
        }
    }

    private static String gatherTaskNames() {
        final StringBuilder builder = new StringBuilder();
        for (JQATask task : tasks.values()) {
            builder.append(task.getName()).append(" ");
        }
        return builder.toString().trim();
    }

    private static void interpretCommandLine(final String[] arg) throws IOException {
        final CommandLineParser parser = new BasicParser();
        Options option = gatherOptions();
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(option, arg);
        } catch (ParseException e) {
            printUsage(option, e.getMessage());
            System.exit(1);
        }
        List<String> requestedTasks = commandLine.getArgList();
        if (requestedTasks.isEmpty()) {
            printUsage(option, "A task must be specified, i.e. one  of " + gatherTaskNames());
        } else {
            for (String requestedTask : requestedTasks) {
                executeTask(requestedTask, option, commandLine);
            }
        }
    }

    private static void executeTask(String taskName, Options option, CommandLine commandLine) throws IOException {
        final JQATask task = tasks.get(taskName);
        try {
            task.withStandardOptions(commandLine);
            task.withOptions(commandLine);
        } catch (MissingConfigurationParameterException e) {
            printUsage(option, e.getMessage());
            System.exit(1);
        }
        final Map<String, Object> properties = readProperties(commandLine);
        task.initialize(properties);
        task.run();
    }

    private static Map<String, Object> readProperties(CommandLine commandLine) throws IOException {
        final Properties properties = new Properties();
        InputStream propertiesStream;
        if (commandLine.hasOption("p")) {
            File propertyFile = new File(commandLine.getOptionValue("p"));
            if (!propertyFile.exists()) {
                throw new IOException("Property file given by command line does not exist: " + propertyFile.getAbsolutePath());
            }
            propertiesStream = new FileInputStream(propertyFile);
        } else {
            propertiesStream = Main.class.getResourceAsStream("/jqassistant.properties");
        }
        Map<String, Object> result = new HashMap<>();
        if (propertiesStream != null) {
            properties.load(propertiesStream);
            for (String name : properties.stringPropertyNames()) {
                result.put(name, properties.getProperty(name));
            }
        }
        return result;
    }

    private static void printUsage(final Options option, final String errorMessage) {
        System.out.println(errorMessage);
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(Main.class.getCanonicalName(), option);
        System.out.println("Example: " + Main.class.getCanonicalName() + " scan -d target/classes,target/test-classes");
    }

    private static void putTasksIntoMap(final List<AbstractJQATask> tasks) {
        for (AbstractJQATask task : tasks) {
            Main.tasks.put(task.taskName, task);
        }
    }

}
