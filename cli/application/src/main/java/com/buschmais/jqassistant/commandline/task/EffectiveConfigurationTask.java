package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationSerializer;
import com.buschmais.jqassistant.core.runtime.impl.configuration.ConfigurationSerializerImpl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.Options;

@Slf4j
public class EffectiveConfigurationTask extends AbstractAnalyzeTask {

    private final ConfigurationSerializer<CliConfiguration> configurationSerializer = new ConfigurationSerializerImpl<>();

    @Override
    public void run(CliConfiguration configuration, Options options) throws CliExecutionException {
        log.info("Effective configuration:\n{}", configurationSerializer.toYaml(configuration));
    }
}
