package com.buschmais.jqassistant.core.runtime.api.plugin;

import java.util.Comparator;
import java.util.Optional;

/**
 * The {@code PluginInfo} class provides common information on
 * a plugin known by jQA.
 *
 * @author Oliver B. Fischer, Freiheitsgrade Consulting
 */
public interface PluginInfo {
    String getName();

    String getId();

    Optional<String> getVersion();

    /**
     * Comparator to compare two given plugin information based on their name,
     * ignoring case differences.
     */
    PluginInfoComparator NAME_COMPARATOR = (left, right) -> left.getName()
        .compareToIgnoreCase(right.getName());

    /**
     * Comparator to compare two given plugin information based on their id,
     * ignoring case differences.
     */
    PluginInfoComparator ID_COMPARATOR = (left, right) -> left.getId()
        .compareToIgnoreCase(right.getId());

    interface PluginInfoComparator extends Comparator<PluginInfo> {
    }
}
