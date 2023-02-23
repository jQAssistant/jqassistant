package com.buschmais.jqassistant.commandline.task;

import java.util.List;

import com.buschmais.jqassistant.commandline.Main;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * @author Oliver B. Fischer, Freiheitsgrade Consulting
 */
public class HelpTask extends AbstractTask {

    @Override
    protected void addTaskOptions(List<Option> options) {
    }

    @Override
    public void run(CliConfiguration configuration, Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(120);
        formatter.printHelp(Main.class.getCanonicalName() + " <task> [options]", options);
        System.out.println("Tasks are: " + gatherTaskNames());
        System.out.println("Example: " + Main.class.getCanonicalName() + " scan -f java:classpath::target/classes java:classpath::target/test-classes");
    }

    /**
     * Returns a string containing the names of all supported tasks.
     *
     * @return The names of all supported tasks.
     */
    private String gatherTaskNames() {
        final StringBuilder builder = new StringBuilder();
        for (String taskName : RegisteredTask.getTaskNames()) {
            builder.append("'")
                .append(taskName)
                .append("' ");
        }
        return builder.toString()
            .trim();
    }

}
