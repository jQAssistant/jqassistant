package com.buschmais.jqassistant.scm.maven.provider;

import java.util.Map;

import javax.inject.Singleton;

import org.apache.maven.plugin.MojoExecutionException;

import com.buschmais.jqassistant.core.plugin.api.*;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginRepositoryImpl;

@Singleton
public class PluginRepositoryProvider {

    private PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl();

    private PluginRepository pluginRepository;

    PluginRepositoryProvider() throws MojoExecutionException {
        pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
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
     * @param properties
     *            The properties.
     * @return The scanner plugin repository.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *             If the repository cannot be created.
     */
    public ScannerPluginRepository getScannerPluginRepository(Map<String, Object> properties) throws MojoExecutionException {
        try {
            return pluginRepository.getScannerPluginRepository(properties);
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
    public ReportPluginRepository getReportPluginRepository(Map<String, Object> properties) throws MojoExecutionException {
        try {
            return pluginRepository.getReportPluginRepository(properties);
        } catch (PluginRepositoryException e) {
            throw new MojoExecutionException("Cannot create rule plugin repository.", e);
        }
    }
}
