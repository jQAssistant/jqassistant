package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.util.function.Supplier;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.maven.configuration.Maven;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;
import com.buschmais.jqassistant.scm.maven.provider.CachingStoreProvider;

import lombok.RequiredArgsConstructor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

@RequiredArgsConstructor
public abstract class AbstractMavenStoreTask extends AbstractMavenTask {

    public static final String STORE_DIRECTORY = "jqassistant/store";

    private final CachingStoreProvider cachingStoreProvider;

    @Override
    public final void prepareProject(MavenTaskContext mavenTaskContext) throws MojoExecutionException, MojoFailureException {
        if (isResetStoreBeforeExecution(mavenTaskContext.getConfiguration())) {
            withStore(Store::reset, mavenTaskContext);
        }
    }

    /**
     * Determine if the store shall be reset before execution of the mojo,can be overwritten by subclasses.
     *
     * @return `true` if the store shall be reset.
     */
    protected boolean isResetStoreBeforeExecution(MavenConfiguration configuration) {
        return false;
    }

    /**
     * Execute an operation with the store.
     * <p>
     * This method enforces thread safety based on the store factory.
     *
     * @param storeOperation
     *     The store.
     * @throws MojoExecutionException
     *     On execution errors.
     * @throws MojoFailureException
     *     On execution failures.
     */
    protected final void withStore(StoreOperation storeOperation, MavenTaskContext mavenTaskContext) throws MojoExecutionException, MojoFailureException {
        MavenConfiguration configuration = mavenTaskContext.getConfiguration();
        Store store = getStore(mavenTaskContext, () -> new File(mavenTaskContext.getRootModule()
            .getBuild()
            .getDirectory(), STORE_DIRECTORY));
        try {
            storeOperation.run(store);
        } finally {
            releaseStore(store, configuration.maven());
        }
    }

    /**
     * Determine the store instance to use for the given root module.
     *
     * @return The store instance.
     * @throws MojoExecutionException
     *     If the store cannot be opened.
     */
    private Store getStore(MavenTaskContext mavenTaskContext, Supplier<File> storeDirectorySupplier) throws MojoExecutionException {
        Object existingStore = cachingStoreProvider.getStore(mavenTaskContext, mavenTaskContext.getPluginRepository(), storeDirectorySupplier);
        if (!Store.class.isAssignableFrom(existingStore.getClass())) {
            throw new MojoExecutionException(
                "Cannot re-use store instance from reactor. Either declare the plugin as extension or execute Maven using the property -D" + Maven.REUSE_STORE
                    + "=false on the command line.");
        }
        return (Store) existingStore;
    }

    /**
     * Release a store instance.
     *
     * @param store
     *     The store instance.
     * @param maven
     *     The {@link Maven} configuration.
     */
    private void releaseStore(Store store, Maven maven) {
        if (!maven.reuseStore()) {
            cachingStoreProvider.closeStore(store);
        }
    }

    /**
     * Defines an operation to execute on an initialized store instance.
     */
    protected interface StoreOperation {
        /**
         * Execute the operation.
         *
         * @param store
         *     The store.
         * @throws MojoExecutionException
         *     On execution errors.
         * @throws MojoFailureException
         *     On execution failures.
         */
        void run(Store store) throws MojoExecutionException, MojoFailureException;
    }
}
