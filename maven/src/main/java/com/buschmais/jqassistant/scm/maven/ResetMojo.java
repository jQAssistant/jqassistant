package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;

import org.apache.maven.plugins.annotations.Mojo;

/**
 * Resets the store.
 */
@Mojo(name = "reset", aggregator = true, requiresProject = false, threadSafe = true)
public class ResetMojo extends AbstractMojo {

    @Override
    protected MavenTask getMavenTask() {
        return new AbstractMavenStoreTask(cachingStoreProvider) {

            @Override
            protected boolean isResetStoreBeforeExecution(MavenConfiguration configuration) {
                return true;
            }

        };

    }
}
