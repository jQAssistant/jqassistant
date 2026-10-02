package com.buschmais.jqassistant.commandline.task;

import java.io.File;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.store.api.StoreFactory;

import org.mockito.Mock;

/**
 * Abstract base class for task tests
 */
abstract class AbstractTaskTest {

    public static final File PROJECT_DIRECTORY = new File(".");

    @Mock
    protected CliConfiguration configuration;

    @Mock
    protected PluginRepository pluginRepository;

    @Mock
    protected StoreFactory storeFactory;

}
