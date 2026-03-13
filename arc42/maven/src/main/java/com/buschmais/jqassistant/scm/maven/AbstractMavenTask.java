package com.buschmais.jqassistant.scm.maven;

import java.util.*;

import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import static java.lang.Thread.currentThread;

@Slf4j
public abstract class AbstractMavenTask implements MavenTask {

    @Override
    public void execute(MavenConfiguration configuration, MavenTaskContext mavenTaskContext) throws MojoExecutionException, MojoFailureException {
        if (configuration.skip()) {
            log.info("Skipping execution (required by jQAssistant configuration)");
        } else {
            ClassLoader contextClassLoader = currentThread().getContextClassLoader();
            currentThread().setContextClassLoader(mavenTaskContext.getPluginRepository()
                .getClassLoader());
            try {
                this.execute(mavenTaskContext);
            } finally {
                currentThread().setContextClassLoader(contextClassLoader);
            }
        }
    }

    public void execute(MavenTaskContext mavenTaskContext) throws MojoExecutionException, MojoFailureException {
        Set<MavenProject> executedModules = getExecutedModules(mavenTaskContext);
        if (executedModules.isEmpty()) {
            prepareProject(mavenTaskContext);
            enterProject(mavenTaskContext);
        }

        try {
            if (mavenTaskContext.getConfiguration()
                .maven()
                .module()
                .skip()) {
                log.info("Skipping module.");
            } else {
                enterModule(mavenTaskContext);
            }
        } finally {
            executedModules.add(mavenTaskContext.getCurrentModule());
        }

        if (isLastModule(mavenTaskContext, executedModules)) {
            leaveProject(mavenTaskContext);
        }
    }

    /**
     * Determine the already executed modules for a given root module.
     *
     * @param executionContext
     *     The Mojo execution context.
     * @return The set of already executed modules belonging to the root module.
     */
    private Set<MavenProject> getExecutedModules(MavenTaskContext executionContext) {
        MojoExecution mojoExecution = executionContext.getMojoExecution();
        // Do NOT use a custom class for execution keys, as different modules may use
        // different classloaders
        String executionKey = mojoExecution.getGoal() + "@" + mojoExecution.getExecutionId();
        MavenProject rootModule = executionContext.getRootModule();
        String executedModulesContextKey = AbstractMavenTask.class.getName() + "#executedModules";
        Map<String, Set<MavenProject>> executedProjectsPerExecutionKey = (Map<String, Set<MavenProject>>) executionContext.getRootModule()
            .getContextValue(executedModulesContextKey);
        if (executedProjectsPerExecutionKey == null) {
            executedProjectsPerExecutionKey = new HashMap<>();
            rootModule.setContextValue(executedModulesContextKey, executedProjectsPerExecutionKey);
        }
        return executedProjectsPerExecutionKey.computeIfAbsent(executionKey, k -> new HashSet<>());
    }

    private boolean isLastModule(MavenTaskContext executionContext, Set<MavenProject> executedModules) throws MojoExecutionException {
        Map<MavenProject, List<MavenProject>> projects = executionContext.getProjects();
        MavenProject rootModule = executionContext.getRootModule();
        List<MavenProject> projectModules = projects.get(rootModule);
        MavenProject currentModule = executionContext.getCurrentModule();
        Set<MavenProject> remainingModules = getRemainingModules(executionContext, projectModules);
        remainingModules.removeAll(executedModules);
        remainingModules.remove(currentModule);
        if (remainingModules.isEmpty()) {
            log.debug("Did not find any subsequent module with a plugin configuration, considering this module the last one within the project.");
            return true;
        }
        return false;
    }

    private Set<MavenProject> getRemainingModules(MavenTaskContext mavenTaskContext, List<MavenProject> projectModules) {
        Set<MavenProject> remainingModules = new HashSet<>();
        MojoExecution mojoExecution = mavenTaskContext.getMojoExecution();
        if (mojoExecution.getPlugin()
            .getExecutions()
            .isEmpty()) {
            log.debug("No configured executions found, assuming CLI invocation.");
            remainingModules.addAll(projectModules);
        } else {
            for (MavenProject projectModule : projectModules) {
                if (mavenTaskContext.containsBuildPlugin(projectModule, mojoExecution.getPlugin())) {
                    remainingModules.add(projectModule);
                }
            }
        }
        return remainingModules;
    }
}
