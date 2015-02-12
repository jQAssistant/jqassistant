package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.*;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.maven3.api.model.*;

/**
 * Scans pom.xml files.
 * 
 * @author ronald.kunzmann@buschmais.com
 */
public abstract class AbstractMavenPomScannerPlugin extends AbstractScannerPlugin<FileResource, MavenPomXmlDescriptor> {

    @Override
    public Class<? extends FileResource> getType() {
        return FileResource.class;
    }

    @Override
    public Class<? extends MavenPomXmlDescriptor> getDescriptorType() {
        return MavenPomXmlDescriptor.class;
    }

    /**
     * Adds activation information for the given profile.
     * 
     * @param mavenProfileDescriptor
     *            The profile descriptor.
     * @param activation
     *            The activation information.
     * @param store
     *            The database.
     */
    private void addActivation(MavenProfileDescriptor mavenProfileDescriptor, Activation activation, Store store) {
        if (null == activation) {
            return;
        }
        MavenProfileActivationDescriptor profileActivationDescriptor = store.create(MavenProfileActivationDescriptor.class);
        mavenProfileDescriptor.setActivation(profileActivationDescriptor);

        profileActivationDescriptor.setJdk(activation.getJdk());
        profileActivationDescriptor.setActiveByDefault(activation.isActiveByDefault());

        ActivationFile activationFile = activation.getFile();
        if (null != activationFile) {
            MavenActivationFileDescriptor activationFileDescriptor = store.create(MavenActivationFileDescriptor.class);
            profileActivationDescriptor.setActivationFile(activationFileDescriptor);
            activationFileDescriptor.setExists(activationFile.getExists());
            activationFileDescriptor.setMissing(activationFile.getMissing());
        }
        ActivationOS os = activation.getOs();
        if (null != os) {
            MavenActivationOSDescriptor osDescriptor = store.create(MavenActivationOSDescriptor.class);
            profileActivationDescriptor.setActivationOS(osDescriptor);
            osDescriptor.setArch(os.getArch());
            osDescriptor.setFamily(os.getFamily());
            osDescriptor.setName(os.getName());
            osDescriptor.setVersion(os.getVersion());
        }
        ActivationProperty property = activation.getProperty();
        if (null != property) {
            PropertyDescriptor propertyDescriptor = store.create(PropertyDescriptor.class);
            profileActivationDescriptor.setProperty(propertyDescriptor);
            propertyDescriptor.setName(property.getName());
            propertyDescriptor.setValue(property.getValue());
        }
    }

    /**
     * Adds configuration information.
     * 
     * @param configurableDescriptor
     *            The descriptor for the configured element (Plugin,
     *            PluginExecution).
     * @param config
     *            The configuration information.
     * @param store
     *            The database.
     */
    private void addConfiguration(ConfigurableDescriptor configurableDescriptor, Xpp3Dom config, Store store) {
        if (null == config) {
            return;
        }
        MavenConfigurationDescriptor configDescriptor = store.create(MavenConfigurationDescriptor.class);
        configurableDescriptor.setConfiguration(configDescriptor);
        Xpp3Dom[] children = config.getChildren();
        for (Xpp3Dom child : children) {
            configDescriptor.getValues().add(getConfigChildNodes(child, store));
        }
    }

    /**
     * Adds information about artifact dependencies.
     * 
     * @param currentArtifactDescriptor
     *            The descriptor for the current artifact.
     * @param dependencies
     *            The dependencies information.
     * @param store
     *            The database.
     */
    private void addDependencies(MavenArtifactDescriptor currentArtifactDescriptor, List<Dependency> dependencies, Store store) {
        for (Dependency dependency : dependencies) {
            MavenArtifactDescriptor dependencyArtifactDescriptor = createMavenArtifactDescriptor(dependency, store);
            DependsOnDescriptor dependsOnDescriptor = store.create(currentArtifactDescriptor, DependsOnDescriptor.class, dependencyArtifactDescriptor);
            dependsOnDescriptor.setOptional(dependency.isOptional());
            dependsOnDescriptor.setScope(dependency.getScope());
        }
    }

    /**
     * Adds information about execution goals.
     * 
     * @param executionDescriptor
     *            The descriptor for the execution.
     * @param pluginExecution
     *            The PluginExecution.
     * @param store
     *            The database.
     */
    private void addExecutionGoals(MavenPluginExecutionDescriptor executionDescriptor, PluginExecution pluginExecution, Store store) {
        List<String> goals = pluginExecution.getGoals();
        for (String goal : goals) {
            MavenExecutionGoalDescriptor goalDescriptor = store.create(MavenExecutionGoalDescriptor.class);
            goalDescriptor.setName(goal);
            executionDescriptor.getGoals().add(goalDescriptor);
        }

    }

    /**
     * Adds information about references licenses.
     * 
     * @param pomDescriptor
     *            The descriptor for the current POM.
     * @param model
     *            The Maven Model.
     * @param store
     *            The database.
     */
    private void addLicenses(MavenPomXmlDescriptor pomDescriptor, Model model, Store store) {
        List<License> licenses = model.getLicenses();
        for (License license : licenses) {
            MavenLicenseDescriptor licenseDescriptor = store.create(MavenLicenseDescriptor.class);
            licenseDescriptor.setUrl(license.getUrl());
            licenseDescriptor.setComments(license.getComments());
            licenseDescriptor.setName(license.getName());
            licenseDescriptor.setDistribution(license.getDistribution());

            pomDescriptor.getLicenses().add(licenseDescriptor);
        }
    }

    /**
     * Adds dependency management information.
     * 
     * @param pomDescriptor
     *            The descriptor for the current POM.
     * @param dependencyManagement
     *            The dependency management information.
     * @param store
     *            The database.
     */
    private void addManagedDependencies(BaseProfileDescriptor pomDescriptor, DependencyManagement dependencyManagement, Store store,
            Class<? extends BaseDependencyDescriptor> relationClass) {
        if (null == dependencyManagement) {
            return;
        }
        List<Dependency> dependencies = dependencyManagement.getDependencies();
        for (Dependency dependency : dependencies) {
            MavenArtifactDescriptor mavenArtifactDescriptor = createMavenArtifactDescriptor(dependency, store);

            BaseDependencyDescriptor managesDependencyDescriptor = store.create(pomDescriptor, relationClass, mavenArtifactDescriptor);
            managesDependencyDescriptor.setOptional(dependency.isOptional());
            managesDependencyDescriptor.setScope(dependency.getScope());
        }

    }

    /**
     * Adds information about managed plugins.
     * 
     * @param pomDescriptor
     *            The descriptor for the current POM.
     * @param build
     *            Information required to build the project.
     * @param store
     *            The database.
     */
    private void addManagedPlugins(BaseProfileDescriptor pomDescriptor, BuildBase build, Store store) {
        if (null == build) {
            return;
        }
        PluginManagement pluginManagement = build.getPluginManagement();
        if (null == pluginManagement) {
            return;
        }
        List<Plugin> plugins = pluginManagement.getPlugins();
        for (Plugin plugin : plugins) {
            MavenPluginDescriptor mavenPluginDescriptor = store.create(MavenPluginDescriptor.class, plugin.getId());
            mavenPluginDescriptor.setGroup(plugin.getGroupId());
            mavenPluginDescriptor.setName(plugin.getArtifactId());
            mavenPluginDescriptor.setVersion(plugin.getVersion());
            mavenPluginDescriptor.setInherited(plugin.isInherited());
            pomDescriptor.getManagedPlugins().add(mavenPluginDescriptor);
            addDependencies(mavenPluginDescriptor, plugin.getDependencies(), store);
            addPluginExecutions(mavenPluginDescriptor, plugin, store);
            addConfiguration(mavenPluginDescriptor, (Xpp3Dom) plugin.getConfiguration(), store);
        }
    }

    /**
     * Adds information about referenced modules.
     * 
     * @param pomDescriptor
     *            The descriptor for the current POM.
     * @param modules
     *            The modules.
     * @param store
     *            The database.
     */
    private void addModules(BaseProfileDescriptor pomDescriptor, List<String> modules, Store store) {
        for (String module : modules) {
            MavenModuleDescriptor moduleDescriptor = store.create(MavenModuleDescriptor.class);
            moduleDescriptor.setName(module);
            pomDescriptor.getModules().add(moduleDescriptor);
        }

    }

    /**
     * Adds information about parent POM.
     * 
     * @param pomDescriptor
     *            The descriptor for the current POM.
     * @param model
     *            The Maven Model.
     * @param store
     *            The database.
     */
    private void addParent(MavenPomXmlDescriptor pomDescriptor, Model model, Store store) {
        Parent parent = model.getParent();
        if (null != parent) {

            MavenPomDescriptor parentDescriptor = store.create(MavenPomDescriptor.class, parent.getId());
            parentDescriptor.setGroup(parent.getGroupId());
            parentDescriptor.setName(parent.getArtifactId());
            parentDescriptor.setVersion(parent.getVersion());

            pomDescriptor.setParent(parentDescriptor);
        }
    }

    /**
     * Adds information about plugin executions.
     * 
     * @param mavenPluginDescriptor
     *            The descriptor for the plugin.
     * @param plugin
     *            The Plugin.
     * @param store
     *            The database.
     */
    private void addPluginExecutions(MavenPluginDescriptor mavenPluginDescriptor, Plugin plugin, Store store) {
        List<PluginExecution> executions = plugin.getExecutions();
        for (PluginExecution pluginExecution : executions) {
            MavenPluginExecutionDescriptor executionDescriptor = store.create(MavenPluginExecutionDescriptor.class);
            executionDescriptor.setId(pluginExecution.getId());
            executionDescriptor.setPhase(pluginExecution.getPhase());
            executionDescriptor.setInherited(pluginExecution.isInherited());
            mavenPluginDescriptor.getExecutions().add(executionDescriptor);
            addExecutionGoals(executionDescriptor, pluginExecution, store);
            addConfiguration(executionDescriptor, (Xpp3Dom) pluginExecution.getConfiguration(), store);
        }

    }

    /**
     * Adds information about plugins.
     * 
     * @param pomDescriptor
     *            The descriptor for the current POM.
     * @param build
     *            Information required to build the project.
     * @param store
     *            The database.
     */
    private void addPlugins(BaseProfileDescriptor pomDescriptor, BuildBase build, Store store) {
        if (null == build) {
            return;
        }
        List<Plugin> plugins = build.getPlugins();
        for (Plugin plugin : plugins) {
            MavenPluginDescriptor mavenPluginDescriptor = store.create(MavenPluginDescriptor.class, plugin.getId());
            mavenPluginDescriptor.setGroup(plugin.getGroupId());
            mavenPluginDescriptor.setName(plugin.getArtifactId());
            mavenPluginDescriptor.setVersion(plugin.getVersion());
            mavenPluginDescriptor.setInherited(plugin.isInherited());
            pomDescriptor.getPlugins().add(mavenPluginDescriptor);
            addDependencies(mavenPluginDescriptor, plugin.getDependencies(), store);
            addPluginExecutions(mavenPluginDescriptor, plugin, store);
            addConfiguration(mavenPluginDescriptor, (Xpp3Dom) plugin.getConfiguration(), store);
        }
    }

    /**
     * Adds information about profile dependencies.
     * 
     * @param profileDescriptor
     *            The descriptor for the current profile.
     * @param dependencies
     *            The dependencies information.
     * @param store
     *            The database.
     */
    private void addProfileDependencies(MavenProfileDescriptor profileDescriptor, List<Dependency> dependencies, Store store) {
        for (Dependency dependency : dependencies) {
            MavenArtifactDescriptor dependencyArtifactDescriptor = createMavenArtifactDescriptor(dependency, store);
            ProfileDependsOnDescriptor profileDependsOnDescriptor = store.create(profileDescriptor, ProfileDependsOnDescriptor.class,
                    dependencyArtifactDescriptor);
            profileDependsOnDescriptor.setOptional(dependency.isOptional());
            profileDependsOnDescriptor.setScope(dependency.getScope());
        }
    }

    /**
     * Adds information about defined profile.
     * 
     * @param pomDescriptor
     *            The descriptor for the current POM.
     * @param model
     *            The Maven Model.
     * @param store
     *            The database.
     */
    private void addProfiles(MavenPomXmlDescriptor pomDescriptor, Model model, Store store) {
        List<Profile> profiles = model.getProfiles();
        for (Profile profile : profiles) {
            MavenProfileDescriptor mavenProfileDescriptor = store.create(MavenProfileDescriptor.class);
            pomDescriptor.getProfiles().add(mavenProfileDescriptor);
            mavenProfileDescriptor.setId(profile.getId());
            addProperties(mavenProfileDescriptor, profile.getProperties(), store);
            addModules(mavenProfileDescriptor, profile.getModules(), store);
            addPlugins(mavenProfileDescriptor, profile.getBuild(), store);
            addManagedPlugins(mavenProfileDescriptor, profile.getBuild(), store);
            addManagedDependencies(mavenProfileDescriptor, profile.getDependencyManagement(), store, ProfileManagesDependencyDescriptor.class);
            addProfileDependencies(mavenProfileDescriptor, profile.getDependencies(), store);
            addActivation(mavenProfileDescriptor, profile.getActivation(), store);
        }
    }

    /**
     * Adds information about defined properties.
     * 
     * @param pomDescriptor
     *            The descriptor for the current POM.
     * @param properties
     *            The properties information.
     * @param store
     *            The database.
     */
    private void addProperties(BaseProfileDescriptor pomDescriptor, Properties properties, Store store) {
        Set<Entry<Object, Object>> entrySet = properties.entrySet();
        for (Entry<Object, Object> entry : entrySet) {
            PropertyDescriptor propertyDescriptor = store.create(PropertyDescriptor.class);
            propertyDescriptor.setName(entry.getKey().toString());
            propertyDescriptor.setValue(entry.getValue().toString());
            pomDescriptor.getProperties().add(propertyDescriptor);
        }

    }

    /**
     * Creates a MavenArtifactDescriptor and fills it with all information from
     * given dependency.
     * 
     * @param dependency
     *            Dependency.
     * @param store
     *            The database.
     * @return The MavenArtifactDescriptor.
     */
    private MavenArtifactDescriptor createMavenArtifactDescriptor(Dependency dependency, Store store) {
        ArtifactFileDescriptor artifactDescriptor = store.create(ArtifactFileDescriptor.class, dependency.getManagementKey());
        artifactDescriptor.setGroup(dependency.getGroupId());
        artifactDescriptor.setName(dependency.getArtifactId());
        artifactDescriptor.setVersion(dependency.getVersion());
        artifactDescriptor.setClassifier(dependency.getClassifier());
        artifactDescriptor.setType(dependency.getType());
        MavenArtifactDescriptor mavenArtifactDescriptor = store.migrate(artifactDescriptor, MavenArtifactDescriptor.class);
        return mavenArtifactDescriptor;
    }

    /**
     * Returns information about child config entries.
     * 
     * @param node
     *            Current config node.
     * @param store
     *            The database.
     * @return Child config information.
     */
    private ValueDescriptor<?> getConfigChildNodes(Xpp3Dom node, Store store) {
        Xpp3Dom[] children = node.getChildren();
        if (children.length == 0) {
            PropertyDescriptor propertyDescriptor = store.create(PropertyDescriptor.class);
            propertyDescriptor.setName(node.getName());
            propertyDescriptor.setValue(node.getValue());
            return propertyDescriptor;
        }
        ArrayValueDescriptor childDescriptor = store.create(ArrayValueDescriptor.class);
        childDescriptor.setName(node.getName());
        for (Xpp3Dom child : children) {
            childDescriptor.getValue().add(getConfigChildNodes(child, store));
        }
        return childDescriptor;
    }

    /** {@inheritDoc} */
    @Override
    public MavenPomXmlDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        Model model;
        try (InputStream stream = item.createStream()) {
            MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
            model = mavenXpp3Reader.read(stream);
        } catch (XmlPullParserException e) {
            throw new IOException("Cannot read POM descriptor.", e);
        }
        MavenPomXmlDescriptor pomDescriptor = createDescriptor(scanner);
        Store store = scanner.getContext().getStore();
        pomDescriptor.setFullQualifiedName(model.getId());
        pomDescriptor.setGroup(model.getGroupId());
        pomDescriptor.setName(model.getArtifactId());
        pomDescriptor.setVersion(model.getVersion());
        pomDescriptor.setType(model.getPackaging());

        addParent(pomDescriptor, model, store);
        addProfiles(pomDescriptor, model, store);
        addProperties(pomDescriptor, model.getProperties(), store);
        addModules(pomDescriptor, model.getModules(), store);
        addManagedDependencies(pomDescriptor, model.getDependencyManagement(), store, PomManagesDependencyDescriptor.class);
        addDependencies(pomDescriptor, model.getDependencies(), store);
        addManagedPlugins(pomDescriptor, model.getBuild(), store);
        addPlugins(pomDescriptor, model.getBuild(), store);
        addLicenses(pomDescriptor, model, store);
        return pomDescriptor;
    }

    protected abstract MavenPomXmlDescriptor createDescriptor(Scanner scanner);

}
