package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.*;
import org.apache.maven.model.building.*;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.model.validation.ModelValidator;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.plugin.m2repo.api.ArtifactProvider;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.PomModelBuilder;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.RawModelBuilder;

/**
 * Implementation of a POM model builder which resolves the effective model.
 */
public class EffectiveModelBuilder implements PomModelBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EffectiveModelBuilder.class);

    private ModelResolverImpl modelResolver;
    private RawModelBuilder rawModelBuilder;

    /**
     * Constructor.
     * 
     * @param artifactProvider
     *            The artifact provider.
     */
    public EffectiveModelBuilder(ArtifactProvider artifactProvider) {
        this.modelResolver = new ModelResolverImpl(artifactProvider);
        this.rawModelBuilder = new RawModelBuilder();
    }

    @Override
    public Model getModel(final File pomFile) throws IOException {
        DefaultModelBuilder builder = new DefaultModelBuilderFactory().newInstance();
        ModelBuildingRequest req = new DefaultModelBuildingRequest();
        req.setProcessPlugins(false);
        req.setPomFile(pomFile);
        req.setModelResolver(modelResolver);
        req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        req.setSystemProperties(System.getProperties());
        builder.setModelValidator(new ModelValidatorImpl());
        try {
            return builder.build(req).getEffectiveModel();
        } catch (ModelBuildingException e) {
            LOGGER.warn("Cannot build effective model for " + pomFile.getAbsolutePath(), e);
            return new EffectiveModel(rawModelBuilder.getModel(pomFile));
        }
    }

    /*
     * A custom model validator
     */
    private static class ModelValidatorImpl implements ModelValidator {

        @Override
        public void validateRawModel(Model model, ModelBuildingRequest request, ModelProblemCollector problems) {
        }

        @Override
        public void validateEffectiveModel(Model model, ModelBuildingRequest request, ModelProblemCollector problems) {
            if (problems instanceof ModelProblemCollectorExt) {
                clearProblems(problems, "problems", true);
                clearProblems(problems, "severities", false);
            }
        }

        /**
         * Clear a relevant fields contained in the
         * {@link ModelProblemCollector} to suppress errors.
         * 
         * @param problems
         *            The problems.
         * @param field
         *            The field to clear.
         * @param logValue
         *            <code>true</code> if the value shall be logged.
         */
        private void clearProblems(ModelProblemCollector problems, String field, boolean logValue) {
            try {
                Field problemsList = problems.getClass().getDeclaredField(field);
                problemsList.setAccessible(true);
                Collection<?> value = (Collection<?>) problemsList.get(problems);
                if (!value.isEmpty()) {
                    if (logValue) {
                        LOGGER.warn("Problems have been detected while validating POM model: {}.", value);
                    }
                    value.clear();
                }
            } catch (NoSuchFieldException e) {
                LOGGER.warn("Cannot find field " + field, e);
            } catch (IllegalAccessException e) {
                LOGGER.warn("Cannot access field " + field, e);
            }
        }
    }

    /**
     * A {@link ModelResolver} implementation.
     */
    public class ModelResolverImpl implements ModelResolver {

        private ArtifactProvider artifactProvider;

        /**
         * Constructor.
         */
        public ModelResolverImpl(ArtifactProvider artifactProvider) {
            this.artifactProvider = artifactProvider;
        }

        @Override
        public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException {
            Artifact artifact = new DefaultArtifact(groupId, artifactId, null, "pom", version);
            ArtifactResult artifactResult;
            try {
                artifactResult = artifactProvider.getArtifact(artifact);
            } catch (ArtifactResolutionException e) {
                throw new UnresolvableModelException("Cannot resolve artifact.", groupId, artifactId, version, e);
            }
            final File file = artifactResult.getArtifact().getFile();
            return new FileModelSource(file);
        }

        @Override
        public void addRepository(Repository repository) throws InvalidRepositoryException {

        }

        @Override
        public ModelResolver newCopy() {
            return new ModelResolverImpl(artifactProvider);
        }
    }

    private static class EffectiveModel extends Model {

        private Model delegate;

        public EffectiveModel(Model delegate) {
            this.delegate = delegate;
        }

        @Override
        public void addContributor(Contributor contributor) {
            delegate.addContributor(contributor);
        }

        @Override
        public void addDeveloper(Developer developer) {
            delegate.addDeveloper(developer);
        }

        @Override
        public void addLicense(License license) {
            delegate.addLicense(license);
        }

        @Override
        public void addMailingList(MailingList mailingList) {
            delegate.addMailingList(mailingList);
        }

        @Override
        public void addProfile(Profile profile) {
            delegate.addProfile(profile);
        }

        @Override
        public Model clone() {
            return delegate.clone();
        }

        @Override
        public String getArtifactId() {
            return delegate.getArtifactId();
        }

        @Override
        public Build getBuild() {
            return delegate.getBuild();
        }

        @Override
        public CiManagement getCiManagement() {
            return delegate.getCiManagement();
        }

        @Override
        public List<Contributor> getContributors() {
            return delegate.getContributors();
        }

        @Override
        public String getDescription() {
            return delegate.getDescription();
        }

        @Override
        public List<Developer> getDevelopers() {
            return delegate.getDevelopers();
        }

        @Override
        public String getGroupId() {
            return delegate.getGroupId() != null ? delegate.getGroupId() : getParent().getGroupId();
        }

        @Override
        public String getInceptionYear() {
            return delegate.getInceptionYear();
        }

        @Override
        public IssueManagement getIssueManagement() {
            return delegate.getIssueManagement();
        }

        @Override
        public List<License> getLicenses() {
            return delegate.getLicenses();
        }

        @Override
        public List<MailingList> getMailingLists() {
            return delegate.getMailingLists();
        }

        @Override
        public String getModelEncoding() {
            return delegate.getModelEncoding();
        }

        @Override
        public String getModelVersion() {
            return delegate.getModelVersion();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Organization getOrganization() {
            return delegate.getOrganization();
        }

        @Override
        public String getPackaging() {
            return delegate.getPackaging();
        }

        @Override
        public Parent getParent() {
            return delegate.getParent();
        }

        @Override
        public Prerequisites getPrerequisites() {
            return delegate.getPrerequisites();
        }

        @Override
        public List<Profile> getProfiles() {
            return delegate.getProfiles();
        }

        @Override
        public Scm getScm() {
            return delegate.getScm();
        }

        @Override
        public String getUrl() {
            return delegate.getUrl();
        }

        @Override
        public String getVersion() {
            return delegate.getVersion() != null ? delegate.getVersion() : getParent().getVersion();
        }

        @Override
        public void removeContributor(Contributor contributor) {
            delegate.removeContributor(contributor);
        }

        @Override
        public void removeDeveloper(Developer developer) {
            delegate.removeDeveloper(developer);
        }

        @Override
        public void removeLicense(License license) {
            delegate.removeLicense(license);
        }

        @Override
        public void removeMailingList(MailingList mailingList) {
            delegate.removeMailingList(mailingList);
        }

        @Override
        public void removeProfile(Profile profile) {
            delegate.removeProfile(profile);
        }

        @Override
        public void setArtifactId(String artifactId) {
            delegate.setArtifactId(artifactId);
        }

        @Override
        public void setBuild(Build build) {
            delegate.setBuild(build);
        }

        @Override
        public void setCiManagement(CiManagement ciManagement) {
            delegate.setCiManagement(ciManagement);
        }

        @Override
        public void setContributors(List<Contributor> contributors) {
            delegate.setContributors(contributors);
        }

        @Override
        public void setDescription(String description) {
            delegate.setDescription(description);
        }

        @Override
        public void setDevelopers(List<Developer> developers) {
            delegate.setDevelopers(developers);
        }

        @Override
        public void setGroupId(String groupId) {
            delegate.setGroupId(groupId);
        }

        @Override
        public void setInceptionYear(String inceptionYear) {
            delegate.setInceptionYear(inceptionYear);
        }

        @Override
        public void setIssueManagement(IssueManagement issueManagement) {
            delegate.setIssueManagement(issueManagement);
        }

        @Override
        public void setLicenses(List<License> licenses) {
            delegate.setLicenses(licenses);
        }

        @Override
        public void setMailingLists(List<MailingList> mailingLists) {
            delegate.setMailingLists(mailingLists);
        }

        @Override
        public void setModelEncoding(String modelEncoding) {
            delegate.setModelEncoding(modelEncoding);
        }

        @Override
        public void setModelVersion(String modelVersion) {
            delegate.setModelVersion(modelVersion);
        }

        @Override
        public void setName(String name) {
            delegate.setName(name);
        }

        @Override
        public void setOrganization(Organization organization) {
            delegate.setOrganization(organization);
        }

        @Override
        public void setPackaging(String packaging) {
            delegate.setPackaging(packaging);
        }

        @Override
        public void setParent(Parent parent) {
            delegate.setParent(parent);
        }

        @Override
        public void setPrerequisites(Prerequisites prerequisites) {
            delegate.setPrerequisites(prerequisites);
        }

        @Override
        public void setProfiles(List<Profile> profiles) {
            delegate.setProfiles(profiles);
        }

        @Override
        public void setScm(Scm scm) {
            delegate.setScm(scm);
        }

        @Override
        public void setUrl(String url) {
            delegate.setUrl(url);
        }

        @Override
        public void setVersion(String version) {
            delegate.setVersion(version);
        }

        @Override
        public File getPomFile() {
            return delegate.getPomFile();
        }

        @Override
        public void setPomFile(File pomFile) {
            delegate.setPomFile(pomFile);
        }

        @Override
        public File getProjectDirectory() {
            return delegate.getProjectDirectory();
        }

        @Override
        public void addDependency(Dependency dependency) {
            delegate.addDependency(dependency);
        }

        @Override
        public void addModule(String string) {
            delegate.addModule(string);
        }

        @Override
        public void addPluginRepository(Repository repository) {
            delegate.addPluginRepository(repository);
        }

        @Override
        public void addProperty(String key, String value) {
            delegate.addProperty(key, value);
        }

        @Override
        public void addRepository(Repository repository) {
            delegate.addRepository(repository);
        }

        @Override
        public List<Dependency> getDependencies() {
            return delegate.getDependencies();
        }

        @Override
        public DependencyManagement getDependencyManagement() {
            return delegate.getDependencyManagement();
        }

        @Override
        public DistributionManagement getDistributionManagement() {
            return delegate.getDistributionManagement();
        }

        @Override
        public InputLocation getLocation(Object key) {
            return delegate.getLocation(key);
        }

        @Override
        public List<String> getModules() {
            return delegate.getModules();
        }

        @Override
        public List<Repository> getPluginRepositories() {
            return delegate.getPluginRepositories();
        }

        @Override
        public Properties getProperties() {
            return delegate.getProperties();
        }

        @Override
        public Reporting getReporting() {
            return delegate.getReporting();
        }

        @Override
        public Object getReports() {
            return delegate.getReports();
        }

        @Override
        public List<Repository> getRepositories() {
            return delegate.getRepositories();
        }

        @Override
        public void removeDependency(Dependency dependency) {
            delegate.removeDependency(dependency);
        }

        @Override
        public void removeModule(String string) {
            delegate.removeModule(string);
        }

        @Override
        public void removePluginRepository(Repository repository) {
            delegate.removePluginRepository(repository);
        }

        @Override
        public void removeRepository(Repository repository) {
            delegate.removeRepository(repository);
        }

        @Override
        public void setDependencies(List<Dependency> dependencies) {
            delegate.setDependencies(dependencies);
        }

        @Override
        public void setDependencyManagement(DependencyManagement dependencyManagement) {
            delegate.setDependencyManagement(dependencyManagement);
        }

        @Override
        public void setDistributionManagement(DistributionManagement distributionManagement) {
            delegate.setDistributionManagement(distributionManagement);
        }

        @Override
        public void setLocation(Object key, InputLocation location) {
            delegate.setLocation(key, location);
        }

        @Override
        public void setModules(List<String> modules) {
            delegate.setModules(modules);
        }

        @Override
        public void setPluginRepositories(List<Repository> pluginRepositories) {
            delegate.setPluginRepositories(pluginRepositories);
        }

        @Override
        public void setProperties(Properties properties) {
            delegate.setProperties(properties);
        }

        @Override
        public void setReporting(Reporting reporting) {
            delegate.setReporting(reporting);
        }

        @Override
        public void setReports(Object reports) {
            delegate.setReports(reports);
        }

        @Override
        public void setRepositories(List<Repository> repositories) {
            delegate.setRepositories(repositories);
        }
    }
}
