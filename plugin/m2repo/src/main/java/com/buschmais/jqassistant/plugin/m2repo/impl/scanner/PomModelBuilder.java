package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collection;

import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.model.validation.ModelValidator;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PomModelBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PomModelBuilder.class);

    private ModelResolverImpl modelResolver;
    private MavenXpp3Reader mavenXpp3Reader;

    public PomModelBuilder(ArtifactProvider artifactProvider) {
        this.modelResolver = new ModelResolverImpl(artifactProvider);
        this.mavenXpp3Reader = new MavenXpp3Reader();
    }

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
            return getRawModel(pomFile, e);
        }
    }

    private Model getRawModel(File pomFile, ModelBuildingException e) throws IOException {
        try (InputStream stream = new FileInputStream(pomFile)) {
            return mavenXpp3Reader.read(stream);
        } catch (XmlPullParserException e2) {
            throw new IOException("Cannot read POM descriptor.", e);
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
         * Clear a relevant fields contained in the {@link ModelProblemCollector} to suppress errors.
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
}
