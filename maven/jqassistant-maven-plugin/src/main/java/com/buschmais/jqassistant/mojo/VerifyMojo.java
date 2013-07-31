/**
 * Copyright (C) 2011 tdarby <tim.darby.uk@googlemail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.buschmais.jqassistant.mojo;

import com.buschmais.jqassistant.core.analysis.api.CatalogReader;
import com.buschmais.jqassistant.core.analysis.api.ConstraintAnalyzer;
import com.buschmais.jqassistant.core.analysis.api.RulesReader;
import com.buschmais.jqassistant.core.analysis.catalog.schema.v1.JqassistantCatalog;
import com.buschmais.jqassistant.core.analysis.catalog.schema.v1.ResourcesType;
import com.buschmais.jqassistant.core.analysis.catalog.schema.v1.RulesType;
import com.buschmais.jqassistant.core.analysis.impl.CatalogReaderImpl;
import com.buschmais.jqassistant.core.analysis.impl.ConstraintAnalyzerImpl;
import com.buschmais.jqassistant.core.analysis.impl.RulesReaderImpl;
import com.buschmais.jqassistant.core.model.api.*;
import com.buschmais.jqassistant.report.api.ReportWriter;
import com.buschmais.jqassistant.report.api.ReportWriterException;
import com.buschmais.jqassistant.report.impl.CompositeReportWriter;
import com.buschmais.jqassistant.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.report.impl.XmlReportWriter;
import com.buschmais.jqassistant.store.api.Store;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * @goal verify
 * @phase verify
 * @requiresProject false
 */
public class VerifyMojo extends AbstractStoreMojo {

    public static final String DEFAULT_RULES_DIRECTORY = "jqassistant";

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyMojo.class);

    /**
     * The directory to scan for rules.
     *
     * @parameter expression="${jqassistant.rules.rulesDirectory}"
     */
    protected File rulesDirectory;
    /**
     * The list of constraint group names to be executed.
     *
     * @parameter
     */
    protected List<String> constraintGroups;

    /**
     * The file to write the XML report to.
     *
     * @parameter expression="${jqassistant.report.xml}" default-value="${project.build.directory}/jqassistant/jqassistant-report.xml"
     */
    protected File xmlReportFile;

    private CatalogReader catalogReader = new CatalogReaderImpl();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Map<String, ConstraintGroup> availableConstraintGroups = readRules();
        final List<ConstraintGroup> selectedConstraintGroups = getSelectedConstraintGroups(availableConstraintGroups);
        InMemoryReportWriter inMemoryReportWriter = new InMemoryReportWriter();
        FileWriter xmlReportFileWriter;
        try {
            xmlReportFileWriter = new FileWriter(xmlReportFile);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create XML report file.", e);
        }
        XmlReportWriter xmlReportWriter;
        try {
            xmlReportWriter = new XmlReportWriter(xmlReportFileWriter);
        } catch (ReportWriterException e) {
            throw new MojoExecutionException("Cannot create XML report file writer.", e);
        }
        List<ReportWriter> reportWriters = new LinkedList<ReportWriter>();
        reportWriters.add(inMemoryReportWriter);
        reportWriters.add(xmlReportWriter);
        try {
            final CompositeReportWriter reportWriter = new CompositeReportWriter(reportWriters);
            execute(new StoreOperation<Void, AbstractMojoExecutionException>() {
                @Override
                public Void run(Store store) throws AbstractMojoExecutionException {
                    ConstraintAnalyzer analyzer = new ConstraintAnalyzerImpl(store, reportWriter);
                    try {
                        analyzer.validateConstraints(selectedConstraintGroups);
                    } catch (ReportWriterException e) {
                        throw new MojoExecutionException("Cannot create report.", e);
                    }
                    return null;
                }
            });
        } catch (MojoFailureException e) {
            throw e;
        } catch (MojoExecutionException e) {
            throw e;
        } catch (AbstractMojoExecutionException e) {
            throw new MojoExecutionException("Caught an unsupported exception.", e);
        } finally {
            IOUtils.closeQuietly(xmlReportFileWriter);
        }
        verifyConceptResults(inMemoryReportWriter);
        verifyConstraintViolations(inMemoryReportWriter);
    }

    /**
     * Return the selected constraint groups.
     *
     * @param availableConstraintGroups The available constraint groups as defined by the rules.
     * @return The selected constraint groups.
     * @throws MojoExecutionException If an undefined group is referenced.
     */
    private List<ConstraintGroup> getSelectedConstraintGroups(Map<String, ConstraintGroup> availableConstraintGroups) throws MojoExecutionException {
        final List<ConstraintGroup> selectedConstraintGroups = new ArrayList<ConstraintGroup>();
        if (constraintGroups != null) {
            for (String constraintGroup : constraintGroups) {
                ConstraintGroup group = availableConstraintGroups.get(constraintGroup);
                if (group == null) {
                    throw new MojoExecutionException("The constraint group '" + constraintGroup + "' is not defined.");
                }
                selectedConstraintGroups.add(group);
            }
        } else {
            selectedConstraintGroups.addAll(availableConstraintGroups.values());
        }
        return selectedConstraintGroups;
    }

    /**
     * Reads the available rules from the rules directory and deployed catalogs.
     *
     * @return A {@link Map} containing {@link ConstraintGroup}s identified by their id.
     * @throws MojoExecutionException If the rules cannot be read.
     */
    private Map<String, ConstraintGroup> readRules() throws MojoExecutionException {
        if (rulesDirectory == null) {
            rulesDirectory = new File(basedir.getAbsoluteFile() + File.separator + DEFAULT_RULES_DIRECTORY);
        }
        List<Source> sources = new ArrayList<Source>();
        // read rules from rules directory
        List<File> ruleFiles = readRulesDirectory(rulesDirectory);
        for (File ruleFile : ruleFiles) {
            getLog().debug("Adding rules from file " + ruleFile.getAbsolutePath());
            sources.add(new StreamSource(ruleFile));
        }
        CatalogReader catalogReader = new CatalogReaderImpl();
        for (JqassistantCatalog catalog : catalogReader.readCatalogs()) {
            for (RulesType rulesType : catalog.getRules()) {
                for (ResourcesType resourcesType : rulesType.getResources()) {
                    String directory = resourcesType.getDirectory();
                    for (String resource : resourcesType.getResource()) {
                        StringBuffer fullResource = new StringBuffer();
                        if (directory != null) {
                            fullResource.append(directory);
                        }
                        fullResource.append(resource);
                        URL url = VerifyMojo.class.getResource(fullResource.toString());
                        if (url != null) {
                            try {
                                LOGGER.debug("Adding rules from " + url.toString());
                                InputStream ruleStream = url.openStream();
                                sources.add(new StreamSource(ruleStream));
                            } catch (IOException e) {
                                throw new MojoExecutionException("Cannot open rule URL: " + url.toString(), e);
                            }
                        } else {
                            LOGGER.warn("Cannot read rules from resource '{}'" + resource);
                        }
                    }
                }
            }
        }
        RulesReader rulesReader = new RulesReaderImpl();
        return rulesReader.read(sources);
    }

    /**
     * Retrieves the list of available rules from the rules directory.
     *
     * @param rulesDirectory The rules directory.
     * @return The {@link List} of available rule {@link File}s.
     * @throws MojoExecutionException If the rules directory cannot be read.
     */
    private List<File> readRulesDirectory(File rulesDirectory) throws MojoExecutionException {
        if (rulesDirectory.exists() && !rulesDirectory.isDirectory()) {
            throw new MojoExecutionException(rulesDirectory.getAbsolutePath() + " does not exist or is not a rulesDirectory.");
        }
        getLog().info("Reading rules from rulesDirectory " + rulesDirectory.getAbsolutePath());
        final List<File> ruleFiles = new ArrayList<File>();
        try {
            new DirectoryWalker<File>() {

                @Override
                protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
                    if (!file.isDirectory() && file.getName().endsWith(".xml")) {
                        results.add(file);
                    }
                }

                public void scan(File directory) throws IOException {
                    super.walk(directory, ruleFiles);
                }
            }.scan(rulesDirectory);
            return ruleFiles;
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot read rulesDirectory: " + rulesDirectory.getAbsolutePath(), e);
        }
    }

    /**
     * Verifies the concept results returned by the {@link InMemoryReportWriter}.
     * <p>A warning is logged for each concept which did not return a result (i.e. has not been applied).</p>
     *
     * @param inMemoryReportWriter The {@link InMemoryReportWriter}.
     */
    private void verifyConceptResults(InMemoryReportWriter inMemoryReportWriter) {
        List<Result<Concept>> conceptResults = inMemoryReportWriter.getConceptResults();
        for (Result<Concept> conceptResult : conceptResults) {
            if (conceptResult.getRows().isEmpty()) {
                getLog().warn("Concept '" + conceptResult.getExecutable().getId() + "' returned an empty result.");
            }
        }
    }

    /**
     * Verifies the constraint violations returned by the {@link InMemoryReportWriter}.
     *
     * @param inMemoryReportWriter The {@link InMemoryReportWriter}.
     * @throws MojoFailureException If constraint violations are detected.
     */
    private void verifyConstraintViolations(InMemoryReportWriter inMemoryReportWriter) throws MojoFailureException {
        List<Result<Constraint>> constraintViolations = inMemoryReportWriter.getConstraintViolations();
        int violations = 0;
        for (Result<Constraint> constraintViolation : constraintViolations) {
            if (!constraintViolation.isEmpty()) {
                AbstractExecutable constraint = constraintViolation.getExecutable();
                getLog().error(constraint.getId() + ": " + constraint.getDescription());
                for (Map<String, Object> columns : constraintViolation.getRows()) {
                    StringBuilder message = new StringBuilder();
                    for (Map.Entry<String, Object> entry : columns.entrySet()) {
                        if (message.length() > 0) {
                            message.append(", ");
                        }
                        message.append(entry.getKey());
                        message.append('=');
                        message.append(entry.getValue());
                    }
                    getLog().error("  " + message.toString());
                }
                violations++;
            }
        }
        if (violations > 0) {
            throw new MojoFailureException(violations + " constraints have been violated!");
        }
    }

    /**
     * Returns the {@link File} to write the XML report to.
     *
     * @return The {@link File} to write the XML report to.
     */
    private File getXmlReportFile() {
        xmlReportFile.getParentFile().mkdirs();
        return xmlReportFile;
    }
}