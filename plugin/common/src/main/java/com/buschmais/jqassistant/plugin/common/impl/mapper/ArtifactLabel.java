package com.buschmais.jqassistant.plugin.common.impl.mapper;

import com.buschmais.jqassistant.core.store.api.model.IndexedLabel;

/**
 * Artifact related labels.
 */
public enum ArtifactLabel implements IndexedLabel {

    ARTIFACT;

    @Override
    public boolean isIndexed() {
        return false;
    }
}
