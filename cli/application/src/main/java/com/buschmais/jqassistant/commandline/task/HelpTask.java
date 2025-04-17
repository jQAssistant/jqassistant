package com.buschmais.jqassistant.commandline.task;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.commandline.Main;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.shared.annotation.Description;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * @author Oliver B. Fischer, Freiheitsgrade Consulting
 */
@Slf4j
@Description("Lists all available tasks.")
public class HelpTask extends AbstractTask {

    @Override
    protected void addTaskOptions(List<Option> options) {
    }

    @Override
    public void run(CliConfiguration configuration, Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(120);
        formatter.printHelp(Main.class.getCanonicalName() + " <task> [options]", options);
        log.info("\n---- Available Tasks: ----\n \n{}", gatherTaskNamesAndDescriptions());
        log.info("\nExample: {} scan -f java:classpath::target/classes java:classpath::target/test-classes", Main.class.getCanonicalName());
    }

    /**
     * Returns a string containing the names of all supported tasks.
     *
     * @return The names of all supported tasks.
     */
    private String gatherTaskNamesAndDescriptions() {
        final StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> task  : RegisteredTask.getTaskNamesAndDescriptions().entrySet()) {
            builder.append("\n").append(task.getKey())
                .append("': ").append(task.getValue());
        }
        return builder.toString()
            .trim();
    }

}
