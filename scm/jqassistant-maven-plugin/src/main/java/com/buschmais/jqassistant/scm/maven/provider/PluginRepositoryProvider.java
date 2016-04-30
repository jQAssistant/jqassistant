
package com.buschmais.jqassistant.scm.maven.provider;

import javax.inject.Singleton;

import com.buschmais.jqassistant.core.plugin.api.*;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginRepositoryImpl;

import org.apache.maven.plugin.MojoExecutionException;

@Singleton
public class PluginRepositoryProvider {

    private PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl();

    private PluginRepository pluginRepository;

    PluginRepositoryProvider() throws MojoExecutionException {
        try {
            pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
        } catch (PluginRepositoryException e) {
            throw new MojoExecutionException("Cannot create plugin repository.", e);
        }
    }

    /**
     * Return the model plugin repository.
     *
     * @return The model plugin repository.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *             If the repository cannot be created.
     */
    public ModelPluginRepository getModelPluginRepository() throws MojoExecutionException {
        try {
            return pluginRepository.getModelPluginRepository();
        } catch (PluginRepositoryException e) {
            throw new MojoExecutionException("Cannot create rule plugin repository.", e);
        }
    }

    /**
     * Return the scanner plugin repository.
     *
     * @return The scanner plugin repository.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *             If the repository cannot be created.
     */
    public ScannerPluginRepository getScannerPluginRepository() throws MojoExecutionException {
        try {
            return pluginRepository.getScannerPluginRepository();
        } catch (PluginRepositoryException e) {
            throw new MojoExecutionException("Cannot create rule plugin repository.", e);
        }
    }

    /**
     * Return the scope plugin repository.
     *
     * @return The scope plugin repository.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *             If the repository cannot be created.
     */
    public ScopePluginRepository getScopePluginRepository() throws MojoExecutionException {
        try {
            return pluginRepository.getScopePluginRepository();
        } catch (PluginRepositoryException e) {
            throw new MojoExecutionException("Cannot create scope plugin repository.", e);
        }
    }

    /**
     * Return the rule plugin repository.
     *
     * @return The rule plugin repository.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *             If the repository cannot be created.
     */
    public RulePluginRepository getRulePluginRepository() throws MojoExecutionException {
        try {
            return pluginRepository.getRulePluginRepository();
        } catch (PluginRepositoryException e) {
            throw new MojoExecutionException("Cannot create rule plugin repository.", e);
        }
    }

    /**
     * Return the report plugin repository.
     *
     * @return The report plugin repository.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *             If the repository cannot be created.
     */
    public ReportPluginRepository getReportPluginRepository() throws MojoExecutionException {
        try {
            return pluginRepository.getReportPluginRepository();
        } catch (PluginRepositoryException e) {
            throw new MojoExecutionException("Cannot create rule plugin repository.", e);
        }
    }
}
