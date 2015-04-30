package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

public class PomModelBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PomModelBuilder.class);

    private ModelResolverImpl modelResolver;

    public PomModelBuilder(ArtifactProvider artifactProvider) {
        this.modelResolver = new ModelResolverImpl(artifactProvider);
    }

    public Model getEffectiveModel(final File pomFile) throws IOException {
        DefaultModelBuilder builder = new DefaultModelBuilderFactory().newInstance();
        ModelBuildingRequest req = new DefaultModelBuildingRequest();
        req.setProcessPlugins(false);
        req.setPomFile(pomFile);
        req.setModelResolver(modelResolver);
        req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        builder.setModelValidator(new ModelValidatorImpl());
        try {
            return builder.build(req).getEffectiveModel();
        } catch (ModelBuildingException e) {
            LOGGER.warn("Cannot build effective model for " + pomFile.getAbsolutePath(), e);
            return null;
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
            List<ArtifactResult> artifactResults;
            try {
                artifactResults = artifactProvider.downloadArtifact(artifact);
            } catch (ArtifactResolutionException e) {
                throw new UnresolvableModelException("Cannot resolve artifact.", groupId, artifactId, version, e);
            }
            ArtifactResult artifactResult = artifactResults.get(0);
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
