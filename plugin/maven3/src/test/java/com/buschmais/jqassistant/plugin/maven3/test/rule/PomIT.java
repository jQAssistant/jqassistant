package com.buschmais.jqassistant.plugin.maven3.test.rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ValueDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenConfigurationDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenExecutionGoalDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenLicenseDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenModuleDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPluginDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPluginExecutionDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPropertyDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.PomManagesDependencyDescriptor;

public class PomIT extends AbstractJavaPluginIT {

    /**
     * Scans and tests pom.xml files.
     * 
     * @throws IOException
     *             error during scan
     */
    @Test
    public void testPoms() throws IOException {
        scanClassPathDirectory(getClassesDirectory(PomIT.class));
        store.beginTransaction();

        validateParentPom();
        validateChildPom();

        store.rollbackTransaction();

    }

    /**
     * Validates child pom.
     */
    private void validateChildPom() {
        List<MavenPomXmlDescriptor> pomDescriptors = query("MATCH (n:File:Maven:Xml:Pom) WHERE n.fileName='/child/pom.xml' RETURN n").getColumn("n");
        Assert.assertEquals(1, pomDescriptors.size());

        MavenPomXmlDescriptor pomDescriptor = pomDescriptors.get(0);
        Assert.assertNull(pomDescriptor.getGroup());
        Assert.assertEquals("jqassistant.child", pomDescriptor.getName());
        Assert.assertNull(pomDescriptor.getVersion());

        MavenPomDescriptor parentDescriptor = pomDescriptor.getParent();
        Assert.assertEquals("com.buschmais.jqassistant", parentDescriptor.getGroup());
        Assert.assertEquals("jqassistant.parent", parentDescriptor.getName());
        Assert.assertEquals("1.0.0-RC-SNAPSHOT", parentDescriptor.getVersion());

        // validate dependencies
        List<DependsOnDescriptor> dependencyDescriptors = pomDescriptor.getDependencies();
        Assert.assertEquals(4, dependencyDescriptors.size());
        List<Dependency> dependencyList = createChildDependencies();
        for (Dependency dependency : dependencyList) {
            checkDependency(dependencyDescriptors, dependency);
        }

        Assert.assertEquals(0, pomDescriptor.getProperties().size());
        Assert.assertEquals(0, pomDescriptor.getManagedDependencies().size());
        Assert.assertEquals(0, pomDescriptor.getManagedPlugins().size());
        Assert.assertEquals(0, pomDescriptor.getPlugins().size());
        Assert.assertEquals(0, pomDescriptor.getModules().size());
    }

    /**
     * Validates dependency existence. Fails if no dependency containing all
     * given fields exists.
     * 
     * @param dependencyDescriptors
     *            Descriptors containing all dependencies.
     * @param dependency
     *            expected dependency informations.
     * @param errorMsgPrefix
     *            Prefix for errorMsgs containing context information.
     */
    private void checkDependency(List<DependsOnDescriptor> dependencyDescriptors, Dependency dependency) {
        for (DependsOnDescriptor dependsOnDescriptor : dependencyDescriptors) {
            ArtifactDescriptor dependencyDescriptor = dependsOnDescriptor.getDependency();
            if (Objects.equals(dependencyDescriptor.getGroup(), dependency.group) && //
                    Objects.equals(dependencyDescriptor.getName(), dependency.name) && //
                    Objects.equals(dependencyDescriptor.getClassifier(), dependency.classifier) && //
                    Objects.equals(dependencyDescriptor.getType(), dependency.type) && //
                    Objects.equals(dependencyDescriptor.getVersion(), dependency.version) && //
                    Objects.equals(dependsOnDescriptor.getScope(), dependency.scope)) {
                return;
            }
        }
        Assert.fail("Dependency not found: " + dependency.toString());
    }

    /**
     * Validates dependency existence. Fails if no dependency containing all
     * given fields exists.
     * 
     * @param dependencyDescriptors
     *            Descriptors containing all dependencies.
     * @param dependency
     *            expected dependency informations.
     * @param errorMsgPrefix
     *            Prefix for errorMsgs containing context information.
     */
    private void checkManagedDependency(List<PomManagesDependencyDescriptor> dependencyDescriptors, Dependency dependency) {
        for (PomManagesDependencyDescriptor dependencyRelationDescriptor : dependencyDescriptors) {
            MavenArtifactDescriptor dependencyDescriptor = dependencyRelationDescriptor.getDependency();
            if (Objects.equals(dependencyDescriptor.getGroup(), dependency.group) && //
                    Objects.equals(dependencyDescriptor.getName(), dependency.name) && //
                    Objects.equals(dependencyDescriptor.getClassifier(), dependency.classifier) && //
                    Objects.equals(dependencyDescriptor.getType(), dependency.type) && //
                    Objects.equals(dependencyDescriptor.getVersion(), dependency.version) && //
                    Objects.equals(dependencyRelationDescriptor.getScope(), dependency.scope)) {
                return;
            }
        }
        Assert.fail("Dependency not found: " + dependency.toString());
    }

    /**
     * Validates parent pom.
     */
    private void validateParentPom() {
        List<MavenPomXmlDescriptor> mavenPomDescriptors = query("MATCH (n:File:Maven:Xml:Pom) WHERE n.fileName='/pom.xml' RETURN n").getColumn("n");
        Assert.assertEquals(1, mavenPomDescriptors.size());

        MavenPomXmlDescriptor pomDescriptor = mavenPomDescriptors.iterator().next();
        Assert.assertEquals("com.buschmais.jqassistant", pomDescriptor.getGroup());
        Assert.assertEquals("jqassistant.parent", pomDescriptor.getName());
        Assert.assertEquals("1.0.0-RC-SNAPSHOT", pomDescriptor.getVersion());

        MavenPomDescriptor parentDescriptor = pomDescriptor.getParent();
        Assert.assertNull(parentDescriptor);

        List<MavenLicenseDescriptor> licenseDescriptors = pomDescriptor.getLicenses();
        Assert.assertEquals(1, mavenPomDescriptors.size());
        MavenLicenseDescriptor licenseDescriptor = licenseDescriptors.iterator().next();
        Assert.assertEquals("GNU General Public License, v3", licenseDescriptor.getName());
        Assert.assertEquals("http://www.gnu.org/licenses/gpl-3.0.html", licenseDescriptor.getUrl());

        // dependency management
        List<PomManagesDependencyDescriptor> managedDependencyDescriptors = pomDescriptor.getManagedDependencies();
        List<Dependency> managedDependencies = createManagedParentDependencies();
        Assert.assertEquals(managedDependencies.size(), managedDependencyDescriptors.size());
        for (Dependency dependency : managedDependencies) {
            checkManagedDependency(managedDependencyDescriptors, dependency);
        }

        Assert.assertEquals(0, pomDescriptor.getDependencies().size());

        // properties
        List<MavenPropertyDescriptor> propertyDescriptors = pomDescriptor.getProperties();
        Properties properties = new Properties();
        properties.put("project.build.sourceEncoding", "UTF-8");
        properties.put("org.slf4j_version", "1.7.5");

        Assert.assertEquals(2, propertyDescriptors.size());
        for (MavenPropertyDescriptor mavenPropertyDescriptor : propertyDescriptors) {
            String value = properties.getProperty(mavenPropertyDescriptor.getName());
            Assert.assertNotNull(value);
            Assert.assertEquals(value, mavenPropertyDescriptor.getValue());
        }

        // modules
        List<MavenModuleDescriptor> modules = pomDescriptor.getModules();
        Assert.assertEquals(1, modules.size());
        Assert.assertEquals("child", modules.get(0).getName());

        // plugins
        List<MavenPluginDescriptor> pluginDescriptors = pomDescriptor.getPlugins();
        List<Plugin> plugins = createParentPlugins();
        Assert.assertEquals(plugins.size(), pluginDescriptors.size());
        for (Plugin plugin : plugins) {
            checkPlugin(pluginDescriptors, plugin);
        }

        // managed plugins
        List<MavenPluginDescriptor> managedPluginDescriptors = pomDescriptor.getManagedPlugins();
        List<Plugin> managedPlugins = createManagedParentPlugins();
        Assert.assertEquals(managedPlugins.size(), managedPluginDescriptors.size());
        for (Plugin plugin : managedPlugins) {
            checkPlugin(managedPluginDescriptors, plugin);
        }
    }

    private void checkPlugin(List<MavenPluginDescriptor> managedPluginDescriptors, Plugin plugin) {
        MavenPluginDescriptor mavenPluginDescriptor = validatePlugin(managedPluginDescriptors, plugin);
        checkConfiguration(mavenPluginDescriptor.getConfiguration(), plugin.configuration);
        List<MavenPluginExecutionDescriptor> executionDescriptors = mavenPluginDescriptor.getExecutions();
        Assert.assertEquals(plugin.executions.size(), executionDescriptors.size());
        for (Execution execution : plugin.executions) {
            MavenPluginExecutionDescriptor pluginExecutionDescriptor = validatePluginExecution(executionDescriptors, execution);
            List<MavenExecutionGoalDescriptor> goalDescriptors = pluginExecutionDescriptor.getGoals();
            Assert.assertEquals(execution.goals.size(), goalDescriptors.size());
            for (MavenExecutionGoalDescriptor goalDescriptor : goalDescriptors) {
                Assert.assertTrue("Unexpected goal: " + goalDescriptor.getName(), execution.goals.contains(goalDescriptor.getName()));
            }
            checkConfiguration(pluginExecutionDescriptor.getConfiguration(), execution.configuration);
        }
    }

    private void checkConfiguration(MavenConfigurationDescriptor configurationDescriptor, Configuration configuration) {
        if (null != configuration) {
            Assert.assertNotNull(configurationDescriptor);
            for (ConfigEntry entry : configuration.entries) {
                validateConfigurationEntry(configurationDescriptor.getValues(), entry);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void validateConfigurationEntry(List<ValueDescriptor<?>> descriptors, ConfigEntry entry) {
        for (ValueDescriptor<?> valueDescriptor : descriptors) {
            if (valueDescriptor.getName().equals(entry.name)) {
                if (entry instanceof SimpleConfigEntry) {
                    Assert.assertEquals(((SimpleConfigEntry) entry).value, valueDescriptor.getValue());
                    return;
                }
                List<ConfigEntry> entries = ((ComplexConfigEntry) entry).entries;
                for (ConfigEntry subEntry : entries) {
                    validateConfigurationEntry((List<ValueDescriptor<?>>) valueDescriptor.getValue(), subEntry);
                }
                return;
            }
        }
        Assert.fail("Configuration entry not found: " + entry.name);
    }

    private MavenPluginDescriptor validatePlugin(List<MavenPluginDescriptor> pluginDescriptors, Plugin plugin) {
        for (MavenPluginDescriptor mavenPluginDescriptor : pluginDescriptors) {
            if (Objects.equals(mavenPluginDescriptor.getClassifier(), plugin.classifier) && //
                    Objects.equals(mavenPluginDescriptor.getGroup(), plugin.group) && //
                    Objects.equals(mavenPluginDescriptor.getName(), plugin.name) && //
                    Objects.equals(mavenPluginDescriptor.getType(), plugin.type) && //
                    Objects.equals(mavenPluginDescriptor.getVersion(), plugin.version) && //
                    Objects.equals(mavenPluginDescriptor.isInherited(), plugin.inherited)) {
                return mavenPluginDescriptor;
            }
        }
        Assert.fail("Plugin not found: " + plugin.toString());
        return null;
    }

    private MavenPluginExecutionDescriptor validatePluginExecution(List<MavenPluginExecutionDescriptor> executionDescriptors, Execution execution) {
        for (MavenPluginExecutionDescriptor mavenPluginExecutionDescriptor : executionDescriptors) {
            if (Objects.equals(mavenPluginExecutionDescriptor.getId(), execution.id) && //
                    Objects.equals(mavenPluginExecutionDescriptor.getPhase(), execution.phase) && //
                    Objects.equals(mavenPluginExecutionDescriptor.isInherited(), execution.inherited)) {
                return mavenPluginExecutionDescriptor;
            }
        }

        Assert.fail("Execution not found: " + execution.toString());
        return null;
    }

    private List<Dependency> createChildDependencies() {
        List<Dependency> dependencyList = new ArrayList<>();
        dependencyList.add(createDependency("com.buschmais.jqassistant.core", "jqassistant.core.analysis", "test-jar", "${project.version}", null, "test"));
        dependencyList.add(createDependency("com.buschmais.jqassistant.core", "jqassistant.core.store", "jar", null, null, null));
        dependencyList.add(createDependency("junit", "junit", "jar", null, null, null));
        dependencyList.add(createDependency("org.slf4j", "slf4j-simple", "jar", null, null, null));
        return dependencyList;
    }

    private List<Dependency> createManagedParentDependencies() {
        List<Dependency> dependencyList = new ArrayList<>();
        dependencyList.add(createDependency("com.buschmais.jqassistant.core", "jqassistant.core.store", "jar", "${project.version}", null, null));
        dependencyList.add(createDependency("junit", "junit", "jar", "4.11", null, "test"));
        dependencyList.add(createDependency("org.slf4j", "slf4j-simple", "jar", "${org.slf4j_version}", null, "test"));
        return dependencyList;
    }

    private Dependency createDependency(String group, String artifact, String type, String version, String classifier, String scope) {
        Dependency dependency = new Dependency();
        fillArtifact(dependency, group, artifact, type, version, classifier);
        dependency.scope = scope;
        return dependency;
    }

    private Plugin createPlugin(String group, String artifact, String type, String version, String classifier, boolean inherited) {
        Plugin plugin = new Plugin();
        fillArtifact(plugin, group, artifact, type, version, classifier);
        plugin.inherited = inherited;
        return plugin;
    }

    private void fillArtifact(Artifact artifact, String group, String name, String type, String version, String classifier) {
        artifact.group = group;
        artifact.name = name;
        artifact.type = type;
        artifact.version = version;
        artifact.classifier = classifier;
    }

    private List<Plugin> createParentPlugins() {
        List<Plugin> pluginList = new ArrayList<>();
        pluginList.add(createPlugin("org.apache.maven.plugins", "maven-compiler-plugin", null, "3.1", null, true));
        Plugin javadocPlugin = createPlugin("org.apache.maven.plugins", "maven-javadoc-plugin", null, "2.10.1", null, true);
        Execution attachJavadocExecution = createPluginExecution("attach-javadoc", null, true);
        attachJavadocExecution.goals.add("jar");
        javadocPlugin.executions.add(attachJavadocExecution);
        pluginList.add(javadocPlugin);

        Plugin sourceplugin = createPlugin("org.apache.maven.plugins", "maven-source-plugin", null, "2.2.1", null, true);
        Execution attachSourcesExecution = createPluginExecution("attach-sources", null, true);
        attachSourcesExecution.goals.add("jar-no-fork");
        sourceplugin.executions.add(attachSourcesExecution);
        pluginList.add(sourceplugin);

        Plugin sitePlugin = createPlugin("com.github.github", "site-maven-plugin", null, "0.10", null, true);
        Execution siteExecution = createPluginExecution("github-site", "site-deploy", true);
        sitePlugin.executions.add(siteExecution);
        siteExecution.goals.add("site");
        Configuration executionConf = new Configuration();
        siteExecution.configuration = executionConf;
        executionConf.entries.add(new SimpleConfigEntry("testparam1", "test1"));
        ComplexConfigEntry executionParams = new ComplexConfigEntry("paramlist");
        executionConf.entries.add(executionParams);
        executionParams.entries.add(new SimpleConfigEntry("testparam2", "test2"));
        executionParams.entries.add(new SimpleConfigEntry("testparam3", "test3"));
        Configuration pluginConf = new Configuration();
        sitePlugin.configuration = pluginConf;
        pluginConf.entries.add(new SimpleConfigEntry("message", "Creating site for ${project.artifactId}, ${project.version}"));
        pluginConf.entries.add(new SimpleConfigEntry("path", "${project.distributionManagement.site.url}"));
        pluginConf.entries.add(new SimpleConfigEntry("merge", "true"));
        ComplexConfigEntry entry = new ComplexConfigEntry("paramlist");
        pluginConf.entries.add(entry);
        entry.entries.add(new SimpleConfigEntry("testparam4", "test4"));
        pluginList.add(sitePlugin);

        return pluginList;
    }

    private List<Plugin> createManagedParentPlugins() {
        List<Plugin> pluginList = new ArrayList<>();
        Plugin enforcerPlugin = createPlugin("org.apache.maven.plugins", "maven-enforcer-plugin", null, "1.0", null, true);
        Execution enforcerExecution = createPluginExecution("enforce-maven", "validate", true);
        enforcerExecution.goals.add("enforce");
        enforcerPlugin.executions.add(enforcerExecution);
        pluginList.add(enforcerPlugin);

        Plugin jaxbPlugin = createPlugin("org.jvnet.jaxb2.maven2", "maven-jaxb2-plugin", null, "0.9.0", null, true);
        Execution jaxbDefaultExecution = createPluginExecution("default", null, true);
        jaxbDefaultExecution.goals.add("generate");
        jaxbPlugin.executions.add(jaxbDefaultExecution);
        Configuration config = new Configuration();
        config.entries.add(new SimpleConfigEntry("schemaDirectory", "src/main/resources/META-INF/xsd"));
        ComplexConfigEntry argsEntry = new ComplexConfigEntry("args");
        argsEntry.entries.add(new SimpleConfigEntry("arg", "-Xdefault-value"));
        config.entries.add(argsEntry);
        ComplexConfigEntry pluginsEntry = new ComplexConfigEntry("plugins");
        ComplexConfigEntry pluginEntry = new ComplexConfigEntry("plugin");
        pluginEntry.entries.add(new SimpleConfigEntry("version", "1.1"));
        pluginEntry.entries.add(new SimpleConfigEntry("groupId", "org.jvnet.jaxb2_commons"));
        pluginEntry.entries.add(new SimpleConfigEntry("artifactId", "jaxb2-default-value"));
        pluginsEntry.entries.add(pluginEntry);
        config.entries.add(pluginsEntry);
        jaxbDefaultExecution.configuration = config;
        pluginList.add(jaxbPlugin);

        Plugin jqaMavenPlugin = createPlugin("com.buschmais.jqassistant.scm", "jqassistant-maven-plugin", null, "${project.version}", null, true);
        Execution scanExecution = createPluginExecution("scan", null, true);
        scanExecution.goals.add("scan");
        jqaMavenPlugin.executions.add(scanExecution);
        Execution analyzeExecution = createPluginExecution("analyze", null, true);
        analyzeExecution.goals.add("analyze");
        jqaMavenPlugin.executions.add(analyzeExecution);
        pluginList.add(jqaMavenPlugin);

        pluginList.add(createPlugin("org.apache.maven.plugins", "maven-failsafe-plugin", null, "2.18", null, true));
        Plugin surefirePlugin = createPlugin("org.apache.maven.plugins", "maven-surefire-plugin", null, "2.18", null, true);
        Configuration surefireConfig = new Configuration();
        ComplexConfigEntry includesEntry = new ComplexConfigEntry("includes");
        includesEntry.entries.add(new SimpleConfigEntry("include", "**/*Test.java"));
        surefireConfig.entries.add(includesEntry);
        surefirePlugin.configuration = surefireConfig;
        pluginList.add(surefirePlugin);

        Plugin assemblyPlugin = createPlugin("org.apache.maven.plugins", "maven-assembly-plugin", null, "2.5", null, true);
        Execution asciidocExecution = createPluginExecution("attach-asciidoc", "", true);
        asciidocExecution.goals.add("single");
        assemblyPlugin.executions.add(asciidocExecution);
        Execution distributionExecution = createPluginExecution("attach-distribution", null, true);
        distributionExecution.goals.add("single");
        assemblyPlugin.executions.add(distributionExecution);
        pluginList.add(assemblyPlugin);

        pluginList.add(createPlugin("org.apache.maven.plugins", "maven-jar-plugin", null, "2.4", null, true));
        pluginList.add(createPlugin("org.apache.maven.plugins", "maven-site-plugin", null, "3.3", null, true));

        return pluginList;
    }

    private Execution createPluginExecution(String id, String phase, boolean inherited) {
        Execution execution = new Execution();
        execution.id = id;
        execution.phase = phase;
        execution.inherited = inherited;
        return execution;
    }

    private class Artifact {
        protected String group, name, version, type, classifier;
    }

    private class Dependency extends Artifact {
        private String scope;

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return String.format("%s:%s:%s:%s:%s:%s", group, name, type, version, classifier, scope);
        }
    }

    private class Plugin extends Artifact {
        private boolean inherited;
        private Configuration configuration;
        private List<Execution> executions = new ArrayList<>();

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return String.format("%s:%s:%s:%s:%s:%s", group, name, type, version, classifier, inherited);
        }
    }

    private class Execution {
        private String id, phase;
        private boolean inherited;
        private Configuration configuration;
        private List<String> goals = new ArrayList<>();

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return String.format("%s:%s:%s", id, phase, inherited);
        }
    }

    private class Configuration {
        private List<ConfigEntry> entries = new ArrayList<>();
    }

    private class ConfigEntry {
        private ConfigEntry(String name) {
            this.name = name;
        }

        protected String name;
    }

    private class SimpleConfigEntry extends ConfigEntry {
        private SimpleConfigEntry(String name, String value) {
            super(name);
            this.value = value;
        }

        private String value;
    }

    private class ComplexConfigEntry extends ConfigEntry {
        private ComplexConfigEntry(String name) {
            super(name);
        }

        private List<ConfigEntry> entries = new ArrayList<>();
    }
}
