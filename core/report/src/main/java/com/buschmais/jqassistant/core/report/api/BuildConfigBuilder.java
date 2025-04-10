package com.buschmais.jqassistant.core.report.api;

import java.time.ZonedDateTime;

import com.buschmais.jqassistant.core.report.api.configuration.Build;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationBuilder;

import lombok.NoArgsConstructor;
import org.eclipse.microprofile.config.spi.ConfigSource;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class BuildConfigBuilder {

    public static ConfigSource getConfigSource(String name, ZonedDateTime timestamp) {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder("BuildConfigSource", 50);
        configurationBuilder.with(Build.class, Build.NAME, name)
            .with(Build.class, Build.TIMESTAMP, ISO_OFFSET_DATE_TIME.format(timestamp));
        return configurationBuilder.build();
    }

}
