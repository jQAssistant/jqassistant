package com.buschmais.jqassistant.mojo;

import com.buschmais.jqassistant.core.analysis.api.CatalogReader;
import com.buschmais.jqassistant.core.analysis.api.RulesReader;
import com.buschmais.jqassistant.core.analysis.catalog.schema.v1.JqassistantCatalog;
import com.buschmais.jqassistant.core.analysis.catalog.schema.v1.ResourcesType;
import com.buschmais.jqassistant.core.analysis.catalog.schema.v1.RulesType;
import com.buschmais.jqassistant.core.analysis.impl.CatalogReaderImpl;
import com.buschmais.jqassistant.core.analysis.impl.RulesReaderImpl;
import com.buschmais.jqassistant.core.model.api.ConstraintGroup;
import org.apache.commons.io.DirectoryWalker;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Abstract base implementation for analysis MOJOs.
 */
public abstract class AbstractAnalysisMojo extends AbstractStoreMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAnalysisMojo.class);
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
     * Return the selected constraint groups.
     *
     * @param availableConstraintGroups The available constraint groups as defined by the rules.
     * @return The selected constraint groups.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          If an undefined group is referenced.
     */
    protected List<ConstraintGroup> getSelectedConstraintGroups(Map<String, ConstraintGroup> availableConstraintGroups) throws MojoExecutionException {
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
     * @return A {@link java.util.Map} containing {@link com.buschmais.jqassistant.core.model.api.ConstraintGroup}s identified by their id.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          If the rules cannot be read.
     */
    protected Map<String, ConstraintGroup> readRules() throws MojoExecutionException {
        if (rulesDirectory == null) {
            rulesDirectory = new File(basedir.getAbsoluteFile() + File.separator + VerifyMojo.DEFAULT_RULES_DIRECTORY);
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
     * @return The {@link java.util.List} of available rule {@link java.io.File}s.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          If the rules directory cannot be read.
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
}
