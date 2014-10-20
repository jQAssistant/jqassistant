package com.buschmais.jqassistant.scm.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.*;

import com.google.common.base.CaseFormat;

/**
 * @author jn4, Kontext E GmbH, 23.01.14
 * @author Dirk Mahler
 */
public class Main {

    /**
     * Define all known tasks.
     */
    private enum Task {
        /**
         * Scan.
         */
        SCAN(new ScanTask()),
        /**
         * Server.
         */
        SERVER(new ServerTask()),
        /**
         * Available rules.
         */
        AVAILABLE_RULES(new AvailableRulesTask()),
        /**
         * Available rules.
         */
        EFFECTIVE_RULES(new EffectiveRulesTask()),
        /**
         * Analyze.
         */
        ANALYZE(new AnalyzeTask()),
        /**
         * Reset.
         */
        RESET(new ResetTask()),
        /**
         * Report.
         */
        REPORT(new ReportTask());

        private JQATask task;

        /**
         * Constructor.
         * 
         * @param task
         */
        private Task(JQATask task) {
            this.task = task;
        }

        public JQATask getTask() {
            return task;
        }
    }

    /**
     * The main method.
     * 
     * @param args
     *            The command line arguments.
     * @throws IOException
     *             If an error occurs.
     */
    public static void main(String[] args) throws IOException {
        try {
            interpretCommandLine(args);
        } catch (JqaConstraintViolationException e) {
            System.out.println(e.getMessage());
            System.exit(2);
        }
    }

    /**
     * Gather all options which are supported by the task (i.e. including
     * standard and specific options).
     * 
     * @return The options.
     */
    private static Options gatherOptions() {
        final Options options = new Options();
        gatherTasksOptions(options);
        gatherStandardOptions(options);
        return options;
    }

    /**
     * Gathers the standard options shared by all tasks.
     * 
     * @param options
     *            The standard options.
     */
    @SuppressWarnings("static-access")
    private static void gatherStandardOptions(final Options options) {
        options.addOption(OptionBuilder.withArgName("p").withDescription("Path to property file; default is jqassistant.properties in the class path")
                .withLongOpt("properties").hasArg().create("p"));
        options.addOption(new Option("help", "print this message"));
    }

    /**
     * Gathers the task specific options for all tasks.
     * 
     * @param options
     *            The task specific options.
     */
    private static void gatherTasksOptions(final Options options) {
        for (Task task : Task.values()) {
            for (Option option : task.getTask().getOptions()) {
                options.addOption(option);
            }
        }
    }

    /**
     * Returns a string containing the names of all supported tasks.
     * 
     * @return The names of all supported tasks.
     */
    private static String gatherTaskNames() {
        final StringBuilder builder = new StringBuilder();
        for (Task task : Task.values()) {
            builder.append(task.name().toLowerCase()).append(" ");
        }
        return builder.toString().trim();
    }

    /**
     * Parse the command line and execute the requested task.
     * 
     * @param arg
     *            The command line.
     * @throws IOException
     *             If an error occurs.
     */
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

    /**
     * Executes a task.
     * 
     * @param taskName
     *            The task name.
     * @param option
     *            The option.
     * @param commandLine
     *            The command line.
     * @throws IOException
     */
    private static void executeTask(String taskName, Options option, CommandLine commandLine) throws IOException {
        String formattedTaskName = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, taskName);
        final JQATask task = Task.valueOf(formattedTaskName).getTask();
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

}
