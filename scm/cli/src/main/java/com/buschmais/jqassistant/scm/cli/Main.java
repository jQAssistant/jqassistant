package com.buschmais.jqassistant.scm.cli;

import java.util.Arrays;
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
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * @author jn4, Kontext E GmbH, 23.01.14
 */
public class Main {
    private static final Map<String, JqAssistantTask> functions = new HashMap<>();

    public static void main(String[] args) {
        StaticLoggerBinder.getSingleton().setLog(new SystemStreamLog());
        putTasksIntoMap(Arrays.asList(
                new ClassToNeo4JImporter(),
                new CmdlineServer(),
                new AnalyzeTask(new Properties()),
                new ResetDatabase()
        ));
        interpretCommandLine(args, gatherOptions());
    }

    private static Options gatherOptions() {
        final Options options = new Options();
        gatherTasksOptions(options);
        gatherStandardOptions(options);
        return options;
    }

    private static void gatherStandardOptions(final Options options) {
        options.addOption(
                OptionBuilder
                        .withArgName("f")
                        .withDescription("Function to be called, one of "+ gatherNamesOfFunctions())
                        .withLongOpt("function")
                        .hasArg()
                        .isRequired()
                        .create("f")
        );
        options.addOption(new Option("help", "print this message"));
    }

    private static void gatherTasksOptions(final Options options) {
        for (OptionsProvider optionsProvider : functions.values()) {
            for (Option option : optionsProvider.getOptions()) {
                options.addOption(option);
            }
        }
    }

    private static String gatherNamesOfFunctions() {
        final StringBuilder builder = new StringBuilder();
        for (JqAssistantTask task : functions.values()) {
            builder.append(task.getName()).append(" ");
        }
        return builder.toString().trim();
    }

    private static void interpretCommandLine(final String[] arg, final Options option) {
        final CommandLineParser parser = new BasicParser();
        try {
            final CommandLine commandLine = parser.parse(option, arg);
            if(commandLine.hasOption("f")) {
                runRequestedTask(option, commandLine);
            } else {
                printUsage(option, "Missing function argument");
                System.exit(1);
            }
        } catch (ParseException e) {
            printUsage(option, e.getMessage());
            System.exit(1);
        }
    }

    private static void runRequestedTask(final Options option, final CommandLine commandLine) {
        final String requestedFunction = commandLine.getOptionValue("f");
        final JqAssistantTask jqAssistantTask = functions.get(requestedFunction);
        if(jqAssistantTask instanceof OptionsConsumer) {
            try {
                ((OptionsConsumer)jqAssistantTask).withOptions(commandLine);
            } catch (MissingConfigurationParameterException e) {
                printUsage(option, e.getMessage());
                System.exit(1);
            }
        }
        jqAssistantTask.run();
    }

    private static void printUsage(final Options option, final String errorMessage) {
        System.out.println(errorMessage);
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(Main.class.getCanonicalName(), option);
        System.out.println("Example: "+Main.class.getCanonicalName()+" -f scan -d cmdline/target/classes,maven/jqassistant-maven-plugin/target/classes");
    }

    private static void putTasksIntoMap(final List<CommonJqAssistantTask> tasks) {
        for (CommonJqAssistantTask task : tasks) {
            functions.put(task.taskName, task);
        }
    }

}
