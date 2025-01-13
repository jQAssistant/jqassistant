package com.buschmais.jqassistant.core.runtime.api.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class VersionProviderTest {

    @Test
    void testGetVersion() {
        String version = VersionProvider.getVersion();
        if (version == null) {
            log.warn("Version should not be null");
        }
    }
}
