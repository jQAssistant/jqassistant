package com.buschmais.jqassistant.plugin.maven3.test.scanner;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import com.buschmais.jqassistant.plugin.maven3.api.model.MavenContributorDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenDeveloperDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenParticipantRoleDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ValueDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.Coordinates;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenActivationFileDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenActivationOSDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenConfigurationDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenDependencyDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenExecutionGoalDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenLicenseDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenModuleDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPluginDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPluginExecutionDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProfileActivationDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProfileDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.PomDependsOnDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.PomManagesDependencyDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.ProfileDependsOnDescriptor;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact.MavenArtifactResolver;

public class MavenPomXmlFileScannerIT extends AbstractJavaPluginIT {

    /**
     * Scans and tests pom.xml files.
     *
     * @throws IOException
     *             error during scan
     */
    @Test
    public void artifactResolver() throws IOException {
        final File directory = getClassesDirectory(MavenPomXmlFileScannerIT.class);
        final ArtifactResolver artifactResolverSpy = Mockito.spy(new MavenArtifactResolver());
        execute(ARTIFACT_ID, new ScanClassPathOperation() {
            @Override
            public List<FileDescriptor> scan(JavaArtifactFileDescriptor artifact, Scanner scanner) {
                ScannerContext context = scanner.getContext();
                context.push(ArtifactResolver.class, artifactResolverSpy);
                scanner.scan(directory, directory.getAbsolutePath(), JavaScope.CLASSPATH);
                context.pop(ArtifactResolver.class);
                return Collections.emptyList();
            }
        });
        verify(artifactResolverSpy, atLeastOnce()).resolve(Mockito.any(Coordinates.class), Mockito.any(ScannerContext.class));
        store.beginTransaction();
        validateParentPom();
        validateChildPom();
        store.commitTransaction();
    }

    /**
     * Scans and tests pom.xml files.
     * 
     * @throws IOException
     *             error during scan
     */
    @Test
    public void pomModel() throws IOException {
        scanClassPathDirectory(getClassesDirectory(MavenPomXmlFileScannerIT.class));
        store.beginTransaction();
        validateParentPom();
        validateChildPom();
        store.commitTransaction();
    }

    /**
     * Verifies that dependencies between two artifacts defined by pom.xml files
     * are resolved to one node.
     * 
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void pomDependencies() throws IOException {
        scanClassPathResource(DefaultScope.NONE, "/dependency/2/pom.xml");
        scanClassPathResource(DefaultScope.NONE, "/dependency/1/pom.xml");
        store.beginTransaction();
        MavenPomDescriptor test1 = store.find(MavenPomDescriptor.class, "com.buschmais.jqassistant:test1:pom:1.0.0-SNAPSHOT");
        assertThat(test1, notNullValue());
        MavenPomDescriptor test2 = store.find(MavenPomDescriptor.class, "com.buschmais.jqassistant:test2:pom:1.0.0-SNAPSHOT");
        assertThat(test2, notNullValue());
        List<PomDependsOnDescriptor> dependencies = test2.getDependencies();
        assertThat(dependencies.size(), equalTo(1));
        PomDependsOnDescriptor dependsOnDescriptor = dependencies.get(0);
        ArtifactDescriptor test1Artifact = store.find(ArtifactDescriptor.class, "com.buschmais.jqassistant:test1:jar:1.0.0-SNAPSHOT");
        assertThat(dependsOnDescriptor.getDependency(), is(test1Artifact));
        store.commitTransaction();
    }

    /**
     * Checks if a Maven plugin has also the label Maven.
     */
    @Test
    public void pluginCanBeFoundWithLabelsMavenAndPlugin() throws Exception {
        scanClassPathDirectory(getClassesDirectory(MavenPomXmlFileScannerIT.class));

        store.beginTransaction();

        // Precondition that parent POM can be found
        List<MavenPomXmlDescriptor> mavenPomDescriptors = query("MATCH (n:File:Maven:Xml:Pom) WHERE n.fileName='/pom.xml' RETURN n").getColumn("n");
        assertEquals(1, mavenPomDescriptors.size());

        // Now let us see if we can find a plugin with given labels Plugin and
        // Maven
        List<Plugin> plugins = query("MATCH (plug:Maven:Plugin) RETURN plug AS p").getColumn("p");

        assertThat(plugins, hasSize(20));

        store.commitTransaction();
    }

    @Test
    public void pluginCanFindMavenPOMInXMLDocumentWithNonStandardName()
            throws PluginRepositoryException, IOException {
        scanClassPathDirectory(getClassesDirectory(MavenPomXmlFileScannerIT.class));

        store.beginTransaction();

        List<MavenPomXmlDescriptor> mavenPomDescriptors = query("MATCH (n:File:Maven:Xml:Pom) WHERE n.fileName=~ \".*/dependency-reduced-pom-file.xml\" RETURN n").getColumn("n");
        assertEquals(1, mavenPomDescriptors.size());

        store.commitTransaction();
    }

    /**
     * Scans an invalid pom.xml file.
     *
     * @throws IOException
     *             error during scan
     */
    @Test
    public void invalidPomFile() throws IOException {
        scanClassPathResource(JavaScope.CLASSPATH,"/invalid/pom.xml");
        store.beginTransaction();
        List<MavenPomXmlDescriptor> mavenPomDescriptors = query("MATCH (n:File:Maven:Xml:Pom) WHERE n.xmlWellFormed=false RETURN n").getColumn("n");
        assertEquals(1, mavenPomDescriptors.size());
        store.commitTransaction();
    }

    /**
     * Checks if all developers in a given pom.xml will be found and
     * added to the model.
     */
    @Test
    public void allDevelopersAreFound() throws Exception {
        scanClassPathResource(JavaScope.CLASSPATH, "/with-developers/pom.xml");

        store.beginTransaction();

        List<MavenPomXmlDescriptor> pomDescriptors = query("MATCH (n:File:Maven:Xml:Pom) " +
                                                           "WHERE n.fileName='/with-developers/pom.xml' " +
                                                           "RETURN n").getColumn("n");

        assertThat(pomDescriptors, hasSize(1));

        MavenPomDescriptor descriptor = pomDescriptors.get(0);

        assertThat(descriptor.getDevelopers(), hasSize(1));

        MavenDeveloperDescriptor developer = descriptor.getDevelopers().stream().findFirst().get();

        assertThat(developer.getId(), equalTo("he"));
        assertThat(developer.getName(), equalTo("Alexej Alexandrowitsch Karenin"));
        assertThat(developer.getOrganization(), equalTo("Tolstoi's World"));
        assertThat(developer.getOrganizationUrl(), equalTo("http://www.tolstoi.org"));
        assertThat(developer.getEmail(), equalTo("aak@tolstoi.org"));
        assertThat(developer.getTimezone(), equalTo("+2"));
        assertThat(developer.getUrl(), equalTo("http://www.tolstoi.org/~aak/"));

        assertThat(developer.getRoles(), hasSize(3));

        List<MavenParticipantRoleDescriptor> roles = developer.getRoles();

        assertThat(roles.stream().map(role -> role.getName()).collect(toList()),
                   containsInAnyOrder("husband", "public officer", "father"));

        List<MavenDeveloperDescriptor> developers = query("MATCH (d:Maven:Developer:Participant) " +
                                                          "WHERE not(d:Contributor) RETURN d")
                                                        .getColumn("d");

        assertThat(developers, hasSize(1));
        assertThat(developers.get(0).getId(), equalTo("he"));
    }

    /**
     * Checks if all contributors in a given pom.xml will be found and
     * added to the model.
     */
    @Test
    public void allContributorsAreFound() throws Exception {
        scanClassPathResource(JavaScope.CLASSPATH, "/with-developers/pom.xml");

        store.beginTransaction();

        List<MavenPomXmlDescriptor> pomDescriptors = query("MATCH (n:File:Maven:Xml:Pom) " +
                                                           "WHERE n.fileName='/with-developers/pom.xml' " +
                                                           "RETURN n").getColumn("n");

        assertThat(pomDescriptors, hasSize(1));

        MavenPomDescriptor descriptor = pomDescriptors.get(0);

        assertThat(descriptor.getContributors(), hasSize(1));

        MavenContributorDescriptor contributor = descriptor.getContributors().stream().findFirst().get();

        assertThat(contributor.getName(), equalTo("Till Eulenspiegel"));
        assertThat(contributor.getOrganization(), equalTo("Familie Eulenspiegel"));
        assertThat(contributor.getOrganizationUrl(), equalTo("http://www.eulenspiegel.org"));
        assertThat(contributor.getEmail(), equalTo("till@eulenspiegel.org"));
        assertThat(contributor.getTimezone(), equalTo("+1"));
        assertThat(contributor.getUrl(), equalTo("http://www.eulenspiegel.org/~till/"));

        assertThat(contributor.getRoles(), hasSize(1));

        List<MavenParticipantRoleDescriptor> roles = contributor.getRoles();

        assertThat(roles.stream().map(role -> role.getName()).collect(toList()),
                   containsInAnyOrder("Narr"));

        List<MavenContributorDescriptor> developers = query("MATCH (c:Maven:Contributor:Participant) " +
                                                            "WHERE not(c:Developer) RETURN c")
            .getColumn("c");

        assertThat(developers, hasSize(1));
        assertThat(developers.get(0).getEmail(), equalTo("till@eulenspiegel.org"));
    }

    /**
     * Validates child pom.
     */
    private void validateChildPom() {
        List<MavenPomXmlDescriptor> pomDescriptors = query("MATCH (n:File:Maven:Xml:Pom) WHERE n.fileName='/child/pom.xml' RETURN n").getColumn("n");
        assertEquals(1, pomDescriptors.size());

        MavenPomXmlDescriptor pomDescriptor = pomDescriptors.get(0);
        Assert.assertNull(pomDescriptor.getGroupId());
        assertEquals("jqassistant.child", pomDescriptor.getArtifactId());
        Assert.assertNull(pomDescriptor.getVersion());

        ArtifactDescriptor parentDescriptor = pomDescriptor.getParent();
        assertEquals("com.buschmais.jqassistant", parentDescriptor.getGroup());
        assertEquals("jqassistant.parent", parentDescriptor.getName());
        assertEquals("1.0.0-RC-SNAPSHOT", parentDescriptor.getVersion());

        // validate dependencies
        List<PomDependsOnDescriptor> dependencyDescriptors = pomDescriptor.getDependencies();
        assertEquals(4, dependencyDescriptors.size());
        List<Dependency> dependencyList = createChildDependencies();
        for (Dependency dependency : dependencyList) {
            checkDependency(dependencyDescriptors, dependency);
        }

        assertEquals(0, pomDescriptor.getProperties().size());
        assertEquals(0, pomDescriptor.getManagedDependencies().size());
        assertEquals(0, pomDescriptor.getManagedPlugins().size());
        assertEquals(0, pomDescriptor.getPlugins().size());
        assertEquals(0, pomDescriptor.getModules().size());
    }

    /**
     * Validates dependency existence. Fails if no dependency containing all
     * given fields exists.
     * 
     * @param dependencyDescriptors
     *            Descriptors containing all dependencies.
     * @param dependency
     *            expected dependency informations.
     */
    private void checkDependency(List<? extends MavenDependencyDescriptor> dependencyDescriptors, Dependency dependency) {
        for (MavenDependencyDescriptor dependsOnDescriptor : dependencyDescriptors) {
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
        assertEquals(1, mavenPomDescriptors.size());

        MavenPomXmlDescriptor pomDescriptor = mavenPomDescriptors.iterator().next();
        assertEquals("com.buschmais.jqassistant", pomDescriptor.getGroupId());
        assertEquals("jqassistant.parent", pomDescriptor.getArtifactId());
        assertEquals("1.0.0-RC-SNAPSHOT", pomDescriptor.getVersion());

        ArtifactDescriptor parentDescriptor = pomDescriptor.getParent();
        Assert.assertNull(parentDescriptor);

        List<MavenLicenseDescriptor> licenseDescriptors = pomDescriptor.getLicenses();
        assertEquals(1, mavenPomDescriptors.size());
        MavenLicenseDescriptor licenseDescriptor = licenseDescriptors.iterator().next();
        assertEquals("GNU General Public License, v3", licenseDescriptor.getName());
        assertEquals("http://www.gnu.org/licenses/gpl-3.0.html", licenseDescriptor.getUrl());

        // dependency management
        List<PomManagesDependencyDescriptor> managedDependencyDescriptors = pomDescriptor.getManagedDependencies();
        List<Dependency> managedDependencies = createManagedParentDependencies();
        assertEquals(managedDependencies.size(), managedDependencyDescriptors.size());
        for (Dependency dependency : managedDependencies) {
            checkManagedDependency(managedDependencyDescriptors, dependency);
        }

        assertEquals(0, pomDescriptor.getDependencies().size());

        // properties
        List<PropertyDescriptor> propertyDescriptors = pomDescriptor.getProperties();
        Properties properties = new Properties();
        properties.put("project.build.sourceEncoding", "UTF-8");
        properties.put("org.slf4j_version", "1.7.5");

        validateProperties(propertyDescriptors, properties);

        // modules
        List<MavenModuleDescriptor> modules = pomDescriptor.getModules();
        assertEquals(1, modules.size());
        assertEquals("child", modules.get(0).getName());

        // plugins
        List<MavenPluginDescriptor> pluginDescriptors = pomDescriptor.getPlugins();
        List<Plugin> plugins = createParentPlugins();
        assertEquals(plugins.size(), pluginDescriptors.size());
        for (Plugin plugin : plugins) {
            checkPlugin(pluginDescriptors, plugin);
        }

        // managed plugins
        List<MavenPluginDescriptor> managedPluginDescriptors = pomDescriptor.getManagedPlugins();
        List<Plugin> managedPlugins = createManagedParentPlugins();
        assertEquals(managedPlugins.size(), managedPluginDescriptors.size());
        for (Plugin plugin : managedPlugins) {
            checkPlugin(managedPluginDescriptors, plugin);
        }

        // profiles
        List<MavenProfileDescriptor> profileDescriptors = pomDescriptor.getProfiles();
        List<Profile> parentProfiles = createParentProfiles();
        assertEquals(2, profileDescriptors.size());
        for (Profile profile : parentProfiles) {
            checkProfile(profileDescriptors, profile);
        }

    }

    private void validateProperties(List<PropertyDescriptor> propertyDescriptors, Properties properties) {
        assertEquals(properties.size(), propertyDescriptors.size());
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String value = properties.getProperty(propertyDescriptor.getName());
            Assert.assertNotNull(value);
            assertEquals(value, propertyDescriptor.getValue());
        }
    }

    /**
     * Validates dependency existence. Fails if no dependency containing all
     * given fields exists.
     * 
     * @param dependencyDescriptors
     *            Descriptors containing all dependencies.
     * @param dependency
     *            expected dependency informations.
     */
    private void checkProfileDependency(List<ProfileDependsOnDescriptor> dependencyDescriptors, Dependency dependency) {
        for (ProfileDependsOnDescriptor dependsOnDescriptor : dependencyDescriptors) {
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

    private void checkProfile(List<MavenProfileDescriptor> profileDescriptors, Profile profile) {
        for (MavenProfileDescriptor mavenProfileDescriptor : profileDescriptors) {
            if (mavenProfileDescriptor.getId().equals(profile.id)) {
                // dependencies
                List<Dependency> dependencies = profile.dependencies;
                for (Dependency dependency : dependencies) {
                    checkProfileDependency(mavenProfileDescriptor.getDependencies(), dependency);
                }
                // modules
                List<MavenModuleDescriptor> modules = mavenProfileDescriptor.getModules();
                assertEquals(profile.modules.size(), modules.size());
                for (MavenModuleDescriptor mavenModuleDescriptor : modules) {
                    Assert.assertTrue(profile.modules.contains(mavenModuleDescriptor.getName()));
                }
                // properties
                validateProperties(mavenProfileDescriptor.getProperties(), profile.properties);
                // plugins
                List<Plugin> plugins = profile.plugins;
                for (Plugin plugin : plugins) {
                    checkPlugin(mavenProfileDescriptor.getPlugins(), plugin);
                }
                // managed plugins
                List<MavenPluginDescriptor> managedPlugins = mavenProfileDescriptor.getManagedPlugins();
                assertEquals(profile.managedPlugins.size(), managedPlugins.size());
                for (Plugin plugin : profile.managedPlugins) {
                    checkPlugin(managedPlugins, plugin);
                }
                checkActivation(mavenProfileDescriptor.getActivation(), profile.activation);
            }
        }

    }

    private void checkActivation(MavenProfileActivationDescriptor activationDescriptor, ProfileActivation activation) {
        if (null != activation) {
            Assert.assertNotNull(activationDescriptor);
            assertEquals(activation.jdk, activationDescriptor.getJdk());
            assertEquals(activation.activeByDefault, activationDescriptor.isActiveByDefault());
            if (null != activation.fileExists || null != activation.fileMissing) {
                MavenActivationFileDescriptor activationFileDescriptor = activationDescriptor.getActivationFile();
                Assert.assertNotNull(activationFileDescriptor);
                assertEquals(activation.fileExists, activationFileDescriptor.getExists());
                assertEquals(activation.fileMissing, activationFileDescriptor.getMissing());
            }
            if (null != activation.propertyName || null != activation.propertyValue) {
                PropertyDescriptor propertyDescriptor = activationDescriptor.getProperty();
                Assert.assertNotNull(propertyDescriptor);
                assertEquals(activation.propertyName, propertyDescriptor.getName());
                assertEquals(activation.propertyValue, propertyDescriptor.getValue());
            }
            if (null != activation.osArch || null != activation.osFamily || null != activation.osName || null != activation.osVersion) {
                MavenActivationOSDescriptor activationOSDescriptor = activationDescriptor.getActivationOS();
                Assert.assertNotNull(activationOSDescriptor);
                assertEquals(activation.osArch, activationOSDescriptor.getArch());
                assertEquals(activation.osFamily, activationOSDescriptor.getFamily());
                assertEquals(activation.osName, activationOSDescriptor.getName());
                assertEquals(activation.osVersion, activationOSDescriptor.getVersion());
            }
        }
    }

    private void checkPlugin(List<MavenPluginDescriptor> managedPluginDescriptors, Plugin plugin) {
        MavenPluginDescriptor mavenPluginDescriptor = validatePlugin(managedPluginDescriptors, plugin);
        checkConfiguration(mavenPluginDescriptor.getConfiguration(), plugin.configuration);
        List<MavenPluginExecutionDescriptor> executionDescriptors = mavenPluginDescriptor.getExecutions();
        assertEquals(plugin.executions.size(), executionDescriptors.size());
        for (Execution execution : plugin.executions) {
            MavenPluginExecutionDescriptor pluginExecutionDescriptor = validatePluginExecution(executionDescriptors, execution);
            List<MavenExecutionGoalDescriptor> goalDescriptors = pluginExecutionDescriptor.getGoals();
            assertEquals(execution.goals.size(), goalDescriptors.size());
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
                    assertEquals(((SimpleConfigEntry) entry).value, valueDescriptor.getValue());
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
        for (MavenPluginDescriptor pluginDescriptor : pluginDescriptors) {
            MavenArtifactDescriptor artifact = pluginDescriptor.getArtifact();
            assertThat(artifact, notNullValue());
            if (Objects.equals(artifact.getClassifier(), plugin.classifier) && //
                    Objects.equals(artifact.getGroup(), plugin.group) && //
                    Objects.equals(artifact.getName(), plugin.name) && //
                    Objects.equals(artifact.getType(), "jar") && //
                    Objects.equals(artifact.getVersion(), plugin.version) && //
                    Objects.equals(pluginDescriptor.isInherited(), plugin.inherited)) {
                return pluginDescriptor;
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
        Dependency dependency = new Dependency(group, artifact, version);
        dependency.type = type;
        dependency.classifier = classifier;
        dependency.scope = scope;
        return dependency;
    }

    private Plugin createPlugin(String group, String artifact, String type, String version, String classifier, boolean inherited) {
        Plugin plugin = new Plugin(group, artifact, version, type);
        plugin.classifier = classifier;
        plugin.inherited = inherited;
        return plugin;
    }

    private List<Plugin> createParentPlugins() {
        List<Plugin> pluginList = new ArrayList<>();
        pluginList.add(createPlugin("org.apache.maven.plugins", "maven-compiler-plugin", null, "3.3", null, true));
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

    private List<Profile> createParentProfiles() {
        List<Profile> profiles = new ArrayList<>();
        Profile itProfile = new Profile("IT");
        profiles.add(itProfile);
        itProfile.dependencies.add(new Dependency("dummyGroup", "dummyArtifact", "dummyVersion"));
        itProfile.managedDependencies.add(new Dependency("dummyManagedGroup", "dummyManagedArtifact", "dummyManagedVersion"));
        // plugin
        Plugin failsafePlugin = new Plugin("org.apache.maven.plugins", "maven-failsafe-plugin", null, null);
        itProfile.plugins.add(failsafePlugin);
        Execution failsafeExecution = new Execution();
        failsafePlugin.executions.add(failsafeExecution);
        failsafeExecution.id = "default";
        failsafeExecution.goals.add("integration-test");
        failsafeExecution.goals.add("verify");
        Configuration failsafeConfig = new Configuration();
        failsafeExecution.configuration = failsafeConfig;
        failsafeConfig.entries.add(new SimpleConfigEntry("argLine", "-Xmx512M"));
        failsafeConfig.entries.add(new SimpleConfigEntry("forkCount", "1"));
        failsafeConfig.entries.add(new SimpleConfigEntry("reuseForks", "true"));
        // managed plugin
        Plugin managedPlugin = new Plugin("org.apache.maven.plugins", "maven-failsafe-plugin", "unknownVersion", null);
        managedPlugin.inherited = false;
        itProfile.managedPlugins.add(managedPlugin);
        // modules
        itProfile.modules.add("childModule1");
        itProfile.modules.add("childModule2");
        // profiles
        itProfile.properties.put("testProperty1", "testValue");
        itProfile.properties.put("testProperty2", "anotherTestValue");
        // activation
        ProfileActivation activation = new ProfileActivation();
        itProfile.activation = activation;
        activation.activeByDefault = true;
        activation.jdk = "1.8";
        activation.propertyName = "activationProperty";
        activation.propertyValue = "activationPropertyValue";
        activation.fileExists = "activate.xml";
        activation.fileMissing = "deactivate.xml";
        activation.osArch = "x86";
        activation.osFamily = "Windows";
        activation.osName = "Windows XP";
        activation.osVersion = "5.1.2600";
        return profiles;
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

    private class Profile {
        private String id;
        private List<String> modules = new ArrayList<>();
        private List<Plugin> plugins = new ArrayList<>();
        private List<Plugin> managedPlugins = new ArrayList<>();
        private List<Dependency> managedDependencies = new ArrayList<>();
        private List<Dependency> dependencies = new ArrayList<>();
        private Properties properties = new Properties();
        private ProfileActivation activation;

        private Profile(String id) {
            this.id = id;
        }
    }

    private class ProfileActivation {
        private boolean activeByDefault = false;
        private String jdk;
        private String propertyName, propertyValue;
        private String fileExists, fileMissing;
        private String osArch, osFamily, osName, osVersion;
    }

    private class Artifact {
        protected String group, name, version, type, classifier;

        private Artifact(String group, String name, String version) {
            this.group = group;
            this.name = name;
            this.version = version;
        }
    }

    private class Dependency extends Artifact {
        private String scope;

        private Dependency(String group, String artifact, String version) {
            super(group, artifact, version);
            super.type = "jar";
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return String.format("%s:%s:%s:%s:%s:%s", group, name, type, version, classifier, scope);
        }
    }

    private class Plugin extends Artifact {
        private boolean inherited = true;
        private Configuration configuration;
        private List<Execution> executions = new ArrayList<>();

        private Plugin(String group, String artifact, String version, String type) {
            super(group, artifact, version);
            super.type = type;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return String.format("%s:%s:%s:%s:%s:%s", group, name, type, version, classifier, inherited);
        }
    }

    private class Execution {
        private String id, phase;
        private boolean inherited = true;
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
