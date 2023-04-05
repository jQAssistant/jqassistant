package com.buschmais.jqassistant.commandline.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.commandline.CliConfigurationException;

import io.smallrye.config.PropertiesConfigSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.eclipse.microprofile.config.spi.ConfigSource;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Slf4j
public class MavenSettingsConfigSourceBuilder {

    private static final String USER_MAVEN_SETTINGS = ".m2/settings.xml";

    public static ConfigSource createConfigSource(File userHome) throws CliConfigurationException {
        Map<String, String> properties = new HashMap<>();
        File settingsFile = new File(userHome, USER_MAVEN_SETTINGS);
        if (settingsFile.exists()) {
            Settings settings = loadMavenSettings(settingsFile);
            String localRepository = settings.getLocalRepository();
            put(properties, Repositories.PREFIX + "." + Repositories.LOCAL, localRepository);
            List<Profile> activeProfiles = getActiveProfiles(settings);
            if (isNotEmpty(activeProfiles)) {
                applyProfileSettings(properties, settings, activeProfiles);
            }
        }
        return new PropertiesConfigSource(properties, "Maven Settings", 90);
    }

    private static void put(Map<String, String> properties, String property, String value) {
        properties.put(property, value);
    }

    private static List<Profile> getActiveProfiles(Settings settings) {
        List<Profile> activeProfiles = settings.getActiveProfiles()
            .stream()
            .map(activeProfile -> settings.getProfilesAsMap()
                .get(activeProfile))
            .filter(profile -> profile != null)
            .collect(toList());
        return activeProfiles;
    }

    private static void applyProfileSettings(Map<String, String> properties, Settings settings, List<Profile> activeProfiles) {
        applyProperties(properties, activeProfiles);
        applyPluginRepositorySettings(properties, settings, activeProfiles);
    }

    private static void applyProperties(Map<String, String> properties, List<Profile> activeProfiles) {
        activeProfiles.stream()
            .map(activeProfile -> activeProfile.getProperties())
            .forEach(activeProfileProperties -> activeProfileProperties.stringPropertyNames()
                .forEach(propertyName -> properties.put(propertyName, activeProfileProperties.getProperty(propertyName))));
    }

    private static void applyPluginRepositorySettings(Map<String, String> properties, Settings settings, List<Profile> activeProfiles) {
        List<Repository> pluginRepositories = activeProfiles.stream()
            .flatMap(activeProfile -> activeProfile.getPluginRepositories()
                .stream())
            .collect(toList());
        int index = 0;
        for (Repository pluginRepository : pluginRepositories) {
            String remotePrefix = Remote.PREFIX + "[" + index + "]" + ".";
            put(properties, remotePrefix + Remote.URL, pluginRepository.getUrl());
            String id = pluginRepository.getId();
            Server server = settings.getServer(id);
            if (server != null) {
                put(properties, remotePrefix + Remote.USERNAME, server.getUsername());
                put(properties, remotePrefix + Remote.PASSWORD, server.getPassword());
            }
            index++;
        }
    }

    private static Settings loadMavenSettings(File settingsFile) throws CliConfigurationException {
        log.info("Using Maven settings from '{}'.", settingsFile);
        SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
        request.setUserSettingsFile(settingsFile);
        request.setSystemProperties(System.getProperties());
        try {
            return new DefaultSettingsBuilderFactory().newInstance()
                .build(request)
                .getEffectiveSettings();
        } catch (SettingsBuildingException ex) {
            throw new CliConfigurationException("Failed to build settings from " + settingsFile, ex);
        }
    }
}
