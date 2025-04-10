package com.buschmais.jqassistant.commandline.task;

import java.util.List;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.shared.annotation.Description;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationSerializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

@Description("Prints the current configuration on the console.")
@Slf4j
public class EffectiveConfigurationTask extends AbstractTask {

    private final ConfigurationSerializer<CliConfiguration> configurationSerializer = new ConfigurationSerializer<>();

    @Override
    protected void addTaskOptions(List<Option> options) {
        // nothing to add here
    }

    @Override
    public void run(CliConfiguration configuration, Options options) throws CliExecutionException {
        log.info("Effective configuration:\n{}", configurationSerializer.toYaml(configuration));
    }

}
