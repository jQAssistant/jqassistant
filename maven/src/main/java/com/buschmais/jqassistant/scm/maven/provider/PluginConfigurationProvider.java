package com.buschmais.jqassistant.scm.maven.provider;

import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.component.annotations.Component;

import com.buschmais.jqassistant.core.plugin.api.ModelPluginRepository;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.ReportPluginRepository;
import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.plugin.api.ScopePluginRepository;
import com.buschmais.jqassistant.core.plugin.impl.ModelPluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.ReportPluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.RulePluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.ScannerPluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.ScopePluginRepositoryImpl;

@Component(role = PluginConfigurationProvider.class, instantiationStrategy = "singleton")
public class PluginConfigurationProvider {

    private PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl();

    /**
     * Return the model plugin repository.
     *
     * @return The model plugin repository.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *             If the repository cannot be created.
     */
    public ModelPluginRepository getModelPluginRepository() throws MojoExecutionException {
        try {
            return new ModelPluginRepositoryImpl(pluginConfigurationReader);
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
            return new ScannerPluginRepositoryImpl(pluginConfigurationReader, properties);
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
            return new ScopePluginRepositoryImpl(pluginConfigurationReader);
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
            return new RulePluginRepositoryImpl(pluginConfigurationReader);
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
            return new ReportPluginRepositoryImpl(pluginConfigurationReader, properties);
        } catch (PluginRepositoryException e) {
            throw new MojoExecutionException("Cannot create rule plugin repository.", e);
        }
    }
}
