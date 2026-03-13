package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.scanner.api.ScopeHelper;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Lists all available scopes.
 */
@Mojo(name = "available-scopes", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
@Slf4j
public class AvailableScopesMojo extends AbstractMojo {

    @Override
    protected MavenTask getMavenTask() {
        return new AbstractMavenTask() {

            @Override
            public void leaveProject(MavenTaskContext mavenTaskContext) {
                log.info("Available scopes for '{}'.", mavenTaskContext.getRootModule()
                    .getName());
                ScopeHelper scopeHelper = new ScopeHelper(log);
                ScannerPluginRepository scannerPluginRepository = mavenTaskContext.getPluginRepository()
                    .getScannerPluginRepository();
                scopeHelper.printScopes(scannerPluginRepository.getScopes());
            }

        };
    }

}
