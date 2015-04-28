package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.*;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.maven3.api.model.*;
import org.apache.maven.model.*;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * Scans pom.xml files.
 * 
 * @author ronald.kunzmann@buschmais.com
 */
public class MavenModelScannerPlugin extends AbstractScannerPlugin<Model, MavenPomXmlDescriptor> {


    @Override
    public void initialize() {
    }

    @Override
    public Class<? extends Model> getType() {
        return Model.class;
    }

    @Override
    public Class<? extends MavenPomXmlDescriptor> getDescriptorType() {
        return MavenPomXmlDescriptor.class;
    }

    @Override
    public boolean accepts(Model item, String path, Scope scope) throws IOException {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MavenPomXmlDescriptor scan(Model model, String path, Scope scope, Scanner scanner) throws IOException {
        MavenPomXmlDescriptor pomDescriptor = createMavenPomXmlDescriptor(model, scanner);
        ScannerContext scannerContext = scanner.getContext();
        Store store = scannerContext.getStore();
        pomDescriptor.setFileName(path);

        addParent(pomDescriptor, model, scannerContext);
        addProfiles(pomDescriptor, model, scannerContext);
        addProperties(pomDescriptor, model.getProperties(), store);
        addModules(pomDescriptor, model.getModules(), store);
        addManagedDependencies(pomDescriptor, model.getDependencyManagement(), scannerContext, PomManagesDependencyDescriptor.class);
        addDependencies(pomDescriptor, model.getDependencies(), PomDependsOnDescriptor.class, scannerContext);
        addManagedPlugins(pomDescriptor, model.getBuild(), scannerContext);
        addPlugins(pomDescriptor, model.getBuild(), scannerContext);
        addLicenses(pomDescriptor, model, store);
        return pomDescriptor;
    }

    /**
     * Create the descriptor and set base information.
     * 
     * @param model
     *            The model.
     * @param scanner
     *            The scanner.
     * @return The descriptor.
     */
    protected MavenPomXmlDescriptor createMavenPomXmlDescriptor(Model model, Scanner scanner) {
        MavenPomXmlDescriptor pomXmlDescriptor = scanner.getContext().getStore().create(MavenPomXmlDescriptor.class);
        pomXmlDescriptor.setName(model.getName());
        pomXmlDescriptor.setGroupId(model.getGroupId());
        pomXmlDescriptor.setArtifactId(model.getArtifactId());
        pomXmlDescriptor.setPackaging(model.getPackaging());
        pomXmlDescriptor.setVersion(model.getVersion());
        pomXmlDescriptor.setFullQualifiedName(model.getId());
        MavenArtifactDescriptor artifact =
                ArtifactResolver.resolve(new ModelCoordinates(model), MavenArtifactDescriptor.class, scanner.getContext());
        pomXmlDescriptor.setDescribes(artifact);
        return pomXmlDescriptor;
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
     *            The descriptor for the configured element (Plugin, PluginExecution).
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
     * @param dependent
     *            The dependent to add artifacts as dependencies
     * @param dependencies
     *            The dependencies information.
     * @param scannerContext
     *            The scanner context
     */
    private <P extends MavenDependentDescriptor, D extends BaseDependencyDescriptor> void addDependencies(P dependent,
            List<Dependency> dependencies, Class<D> dependencyType, ScannerContext scannerContext) {
        for (Dependency dependency : dependencies) {
            MavenArtifactDescriptor dependencyArtifactDescriptor = getMavenArtifactDescriptor(dependency, scannerContext);
            D dependencyDescriptor = scannerContext.getStore().create(dependent, dependencyType, dependencyArtifactDescriptor);
            dependencyDescriptor.setOptional(dependency.isOptional());
            dependencyDescriptor.setScope(dependency.getScope());
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
     * @param scannerContext
     *            The scanner context.
     */
    private void addManagedDependencies(MavenDependentDescriptor pomDescriptor, DependencyManagement dependencyManagement,
            ScannerContext scannerContext, Class<? extends BaseDependencyDescriptor> relationClass) {
        if (null == dependencyManagement) {
            return;
        }
        List<Dependency> dependencies = dependencyManagement.getDependencies();
        addDependencies(pomDescriptor, dependencies, relationClass, scannerContext);
    }

    /**
     * Adds information about managed plugins.
     * 
     * @param pomDescriptor
     *            The descriptor for the current POM.
     * @param build
     *            Information required to build the project.
     * @param scannerContext
     *            The scanner context.
     */
    private void addManagedPlugins(BaseProfileDescriptor pomDescriptor, BuildBase build, ScannerContext scannerContext) {
        if (null == build) {
            return;
        }
        PluginManagement pluginManagement = build.getPluginManagement();
        if (null == pluginManagement) {
            return;
        }
        List<MavenPluginDescriptor> pluginDescriptors = createMavenPluginDescriptors(pluginManagement.getPlugins(), scannerContext);
        pomDescriptor.getManagedPlugins().addAll(pluginDescriptors);
    }

    /**
     * Create plugin descriptors for the given plugins.
     * 
     * @param plugins
     *            The plugins.
     * @param scannerContext
     *            The scanner context.
     * @return The plugin descriptors.
     */
    private List<MavenPluginDescriptor> createMavenPluginDescriptors(List<Plugin> plugins, ScannerContext scannerContext) {
        Store store = scannerContext.getStore();
        List<MavenPluginDescriptor> pluginDescriptors = new ArrayList<>();
        for (Plugin plugin : plugins) {
            MavenPluginDescriptor mavenPluginDescriptor = store.create(MavenPluginDescriptor.class);
            MavenArtifactDescriptor artifactDescriptor =
                    ArtifactResolver.resolve(new PluginCoordinates(plugin), MavenArtifactDescriptor.class, scannerContext);
            mavenPluginDescriptor.setArtifact(artifactDescriptor);
            mavenPluginDescriptor.setInherited(plugin.isInherited());
            addDependencies(mavenPluginDescriptor, plugin.getDependencies(), PluginDependsOnDescriptor.class, scannerContext);
            addPluginExecutions(mavenPluginDescriptor, plugin, store);
            addConfiguration(mavenPluginDescriptor, (Xpp3Dom) plugin.getConfiguration(), store);
            pluginDescriptors.add(mavenPluginDescriptor);
        }
        return pluginDescriptors;
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
     * @param scannerContext
     *            The scanner context.
     */
    private void addParent(MavenPomXmlDescriptor pomDescriptor, Model model, ScannerContext scannerContext) {
        Parent parent = model.getParent();
        if (null != parent) {
            ArtifactDescriptor parentDescriptor =
                    ArtifactResolver.resolve(new ParentCoordinates(parent), ArtifactDescriptor.class, scannerContext);
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
     * @param scannerContext
     *            The scanner context.
     */
    private void addPlugins(BaseProfileDescriptor pomDescriptor, BuildBase build, ScannerContext scannerContext) {
        if (null == build) {
            return;
        }
        List<Plugin> plugins = build.getPlugins();
        List<MavenPluginDescriptor> pluginDescriptors = createMavenPluginDescriptors(plugins, scannerContext);
        pomDescriptor.getPlugins().addAll(pluginDescriptors);
    }

    /**
     * Adds information about profile dependencies.
     * 
     * @param profileDescriptor
     *            The descriptor for the current profile.
     * @param dependencies
     *            The dependencies information.
     * @param scannerContext
     *            The scanner context.
     */
    private void addProfileDependencies(MavenProfileDescriptor profileDescriptor, List<Dependency> dependencies, ScannerContext scannerContext) {
        for (Dependency dependency : dependencies) {
            MavenArtifactDescriptor dependencyArtifactDescriptor = getMavenArtifactDescriptor(dependency, scannerContext);
            ProfileDependsOnDescriptor profileDependsOnDescriptor =
                    scannerContext.getStore().create(profileDescriptor, ProfileDependsOnDescriptor.class, dependencyArtifactDescriptor);
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
     * @param scannerContext
     *            The scanner context.
     */
    private void addProfiles(MavenPomXmlDescriptor pomDescriptor, Model model, ScannerContext scannerContext) {
        List<Profile> profiles = model.getProfiles();
        Store store = scannerContext.getStore();
        for (Profile profile : profiles) {
            MavenProfileDescriptor mavenProfileDescriptor = store.create(MavenProfileDescriptor.class);
            pomDescriptor.getProfiles().add(mavenProfileDescriptor);
            mavenProfileDescriptor.setId(profile.getId());
            addProperties(mavenProfileDescriptor, profile.getProperties(), store);
            addModules(mavenProfileDescriptor, profile.getModules(), store);
            addPlugins(mavenProfileDescriptor, profile.getBuild(), scannerContext);
            addManagedPlugins(mavenProfileDescriptor, profile.getBuild(), scannerContext);
            addManagedDependencies(mavenProfileDescriptor, profile.getDependencyManagement(), scannerContext,
                    ProfileManagesDependencyDescriptor.class);
            addProfileDependencies(mavenProfileDescriptor, profile.getDependencies(), scannerContext);
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
     * Creates a MavenArtifactDescriptor and fills it with all information from given dependency.
     * 
     * @param dependency
     *            Dependency.
     * @param scannerContext
     *            The scanner context.
     * @return The MavenArtifactDescriptor.
     */
    private MavenArtifactDescriptor getMavenArtifactDescriptor(Dependency dependency, ScannerContext scannerContext) {
        MavenArtifactDescriptor artifactDescriptor =
                ArtifactResolver.resolve(new ArtifactResolver.DependencyCoordinates(dependency), MavenArtifactDescriptor.class, scannerContext);
        return artifactDescriptor;
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

    private static final class ModelCoordinates implements ArtifactResolver.Coordinates {

        private Model model;

        public ModelCoordinates(Model model) {
            this.model = model;
        }

        @Override
        public String getGroupId() {
            return model.getGroupId();
        }

        @Override
        public String getArtifactId() {
            return model.getArtifactId();
        }

        @Override
        public String getClassifier() {
            return null;
        }

        @Override
        public String getType() {
            return model.getPackaging();
        }

        @Override
        public String getVersion() {
            return model.getVersion();
        }

        @Override
        public String getId() {
            return model.getId();
        }
    }

    private static final class ParentCoordinates implements ArtifactResolver.Coordinates {
        private Parent parent;

        public ParentCoordinates(Parent parent) {
            this.parent = parent;
        }

        @Override
        public String getGroupId() {
            return parent.getGroupId();
        }

        @Override
        public String getArtifactId() {
            return parent.getArtifactId();
        }

        @Override
        public String getClassifier() {
            return null;
        }

        @Override
        public String getType() {
            return "pom";
        }

        @Override
        public String getVersion() {
            return parent.getVersion();
        }

        @Override
        public String getId() {
            return parent.getId();
        }
    }

    private static final class PluginCoordinates implements ArtifactResolver.Coordinates {

        private Plugin plugin;

        public PluginCoordinates(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public String getGroupId() {
            return plugin.getGroupId();
        }

        @Override
        public String getArtifactId() {
            return plugin.getArtifactId();
        }

        @Override
        public String getClassifier() {
            return null;
        }

        @Override
        public String getType() {
            return null;
        }

        @Override
        public String getVersion() {
            return plugin.getVersion();
        }

        @Override
        public String getId() {
            return plugin.getId();
        }
    }
}
