package com.buschmais.jqassistant.core.resolver.api;

import java.io.File;
import java.util.*;

import com.buschmais.jqassistant.core.resolver.configuration.ArtifactResolverConfiguration;
import com.buschmais.jqassistant.core.resolver.configuration.Policy;
import com.buschmais.jqassistant.core.resolver.configuration.Remote;
import com.buschmais.jqassistant.core.resolver.configuration.Repositories;

import io.smallrye.config.PropertiesConfigSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.settings.*;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.eclipse.microprofile.config.spi.ConfigSource;

import static java.util.stream.Collectors.toList;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MavenSettingsConfigSourceBuilder {

    private static final String USER_MAVEN_SETTINGS = ".m2/settings.xml";

    private static final String DEFAULT_LOCAL_REPOSITORY = ".m2/repository";

    public static ConfigSource createMavenSettingsConfigSource(File userHome, Optional<File> mavenSettingsFile, List<String> profiles) {
        Map<String, String> properties = new HashMap<>();

        File settingsFile = mavenSettingsFile.orElseGet(() -> new File(userHome, USER_MAVEN_SETTINGS));
        if (!settingsFile.exists()) {
            log.info("Maven settings file '{}' does not exist, skipping.", settingsFile);
        } else {
            Settings settings = loadMavenSettings(settingsFile);
            String value = getLocalRepository(settings, userHome);
            put(value, properties, Repositories.PREFIX, Repositories.LOCAL);
            applyMirrors(properties, settings);
            applyProxy(properties, settings);
            applyProfileSettings(properties, settings, getActiveProfiles(settings));
            List<Profile> userProfiles = profiles.stream()
                .map(profile -> settings.getProfilesAsMap()
                    .get(profile))
                .filter(Objects::nonNull)
                .collect(toList());
            applyProfileSettings(properties, settings, userProfiles);
        }
        return new PropertiesConfigSource(properties, "Maven Settings", 90);
    }

    private static String getLocalRepository(Settings settings, File userHome) {
        String localRepository = settings.getLocalRepository();
        if (localRepository != null) {
            return localRepository;
        }
        return new File(userHome, DEFAULT_LOCAL_REPOSITORY).getAbsolutePath();
    }

    private static void applyMirrors(Map<String, String> properties, Settings settings) {
        for (Mirror mirror : settings.getMirrors()) {
            String id = mirror.getId();
            if (id != null) {
                put(mirror.getUrl(), properties, com.buschmais.jqassistant.core.resolver.configuration.Mirror.PREFIX, id,
                    com.buschmais.jqassistant.core.resolver.configuration.Mirror.URL);
                put(mirror.getMirrorOf(), properties, com.buschmais.jqassistant.core.resolver.configuration.Mirror.PREFIX, id,
                    com.buschmais.jqassistant.core.resolver.configuration.Mirror.MIRROR_OF);
                applyServerCredentials(id, settings, properties, com.buschmais.jqassistant.core.resolver.configuration.Mirror.PREFIX,
                    com.buschmais.jqassistant.core.resolver.configuration.Mirror.USERNAME,
                    com.buschmais.jqassistant.core.resolver.configuration.Mirror.PASSWORD);
            } else {
                log.warn("Cannot configure mirror from Maven settings without id (url={}).", mirror.getUrl());
            }
        }
    }

    private static void applyProxy(Map<String, String> properties, Settings settings) {
        settings.getProxies()
            .stream()
            .filter(Proxy::isActive)
            .findFirst()
            .ifPresent(proxy -> {
                put(proxy.getProtocol(), properties, ArtifactResolverConfiguration.PREFIX, ArtifactResolverConfiguration.PROXY,
                    com.buschmais.jqassistant.core.resolver.configuration.Proxy.PROTOCOL);
                put(proxy.getHost(), properties, ArtifactResolverConfiguration.PREFIX, ArtifactResolverConfiguration.PROXY,
                    com.buschmais.jqassistant.core.resolver.configuration.Proxy.HOST);
                put(Integer.toString(proxy.getPort()), properties, ArtifactResolverConfiguration.PREFIX, ArtifactResolverConfiguration.PROXY,
                    com.buschmais.jqassistant.core.resolver.configuration.Proxy.PORT);
                put(proxy.getUsername(), properties, ArtifactResolverConfiguration.PREFIX, ArtifactResolverConfiguration.PROXY,
                    com.buschmais.jqassistant.core.resolver.configuration.Proxy.USERNAME);
                put(proxy.getPassword(), properties, ArtifactResolverConfiguration.PREFIX, ArtifactResolverConfiguration.PROXY,
                    com.buschmais.jqassistant.core.resolver.configuration.Proxy.PASSWORD);
                put(proxy.getNonProxyHosts(), properties, ArtifactResolverConfiguration.PREFIX, ArtifactResolverConfiguration.PROXY,
                    com.buschmais.jqassistant.core.resolver.configuration.Proxy.NON_PROXY_HOSTS);
            });
    }

    private static void put(String value, Map<String, String> properties, String... propertyPath) {
        if (value != null) {
            properties.put(String.join(".", propertyPath), value);
        }
    }

    private static List<Profile> getActiveProfiles(Settings settings) {
        List<Profile> activeProfiles = new ArrayList<>();
        activeProfiles.addAll(settings.getActiveProfiles()
            .stream()
            .map(activeProfile -> settings.getProfilesAsMap()
                .get(activeProfile))
            .filter(Objects::nonNull)
            .collect(toList()));
        activeProfiles.addAll(settings.getProfiles()
            .stream()
            .filter(profile -> {
                Activation activation = profile.getActivation();
                return activation != null && activation.isActiveByDefault();
            })
            .collect(toList()));
        return activeProfiles;
    }

    private static void applyProfileSettings(Map<String, String> properties, Settings settings, List<Profile> profiles) {
        if (!profiles.isEmpty()) {
            applyProperties(properties, profiles);
            applyRepositorySettings(properties, settings, profiles);
        }
    }

    private static void applyProperties(Map<String, String> properties, List<Profile> activeProfiles) {
        activeProfiles.stream()
            .map(Profile::getProperties)
            .forEach(activeProfileProperties -> activeProfileProperties.stringPropertyNames()
                .forEach(propertyName -> properties.put(propertyName, activeProfileProperties.getProperty(propertyName))));
    }

    private static void applyRepositorySettings(Map<String, String> properties, Settings settings, List<Profile> activeProfiles) {
        List<Repository> repositories = new ArrayList<>();
        repositories.addAll(activeProfiles.stream()
            .flatMap(activeProfile -> activeProfile.getRepositories()
                .stream())
            .collect(toList()));
        repositories.addAll(activeProfiles.stream()
            .flatMap(activeProfile -> activeProfile.getPluginRepositories()
                .stream())
            .collect(toList()));
        for (Repository repository : repositories) {
            put(repository.getUrl(), properties, Remote.PREFIX, repository.getId(), Remote.URL);
            applyRepositoryPolicy(repository.getId(), repository.getReleases(), properties, Remote.RELEASES);
            applyRepositoryPolicy(repository.getId(), repository.getSnapshots(), properties, Remote.SNAPSHOTS);
            applyServerCredentials(repository.getId(), settings, properties, Remote.PREFIX, Remote.USERNAME, Remote.PASSWORD);
        }
    }

    private static void applyRepositoryPolicy(String repositoryId, RepositoryPolicy policy, Map<String, String> properties, String policyPrefix) {
        if (policy != null) {
            put(Boolean.toString(policy.isEnabled()), properties, Remote.PREFIX, repositoryId, policyPrefix, Policy.ENABLED);
            put(policy.getUpdatePolicy(), properties, Remote.PREFIX, repositoryId, policyPrefix, Policy.UPDATE_POLICY);
            put(policy.getChecksumPolicy(), properties, Remote.PREFIX, repositoryId, policyPrefix, Policy.CHECKSUM_POLICY);
        }
    }

    private static void applyServerCredentials(String serverId, Settings settings, Map<String, String> properties, String serverPropertyPrefix,
        String serverUsernameProperty, String serverPasswordProperty) {
        Server server = settings.getServer(serverId);
        if (server != null) {
            put(server.getUsername(), properties, serverPropertyPrefix, serverId, serverUsernameProperty);
            put(server.getPassword(), properties, serverPropertyPrefix, serverId, serverPasswordProperty);
        }
    }

    private static Settings loadMavenSettings(File settingsFile) throws IllegalStateException {
        log.info("Using Maven settings from '{}'.", settingsFile);
        SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
        request.setUserSettingsFile(settingsFile);
        request.setSystemProperties(System.getProperties());
        try {
            return new DefaultSettingsBuilderFactory().newInstance()
                .build(request)
                .getEffectiveSettings();
        } catch (SettingsBuildingException ex) {
            throw new IllegalStateException("Failed to build settings from " + settingsFile, ex);
        }
    }
}
