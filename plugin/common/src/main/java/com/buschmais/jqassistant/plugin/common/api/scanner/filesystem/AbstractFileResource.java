package com.buschmais.jqassistant.plugin.common.api.scanner.filesystem;

import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;

@Deprecated
@ToBeRemovedInVersion(major = 2, minor = 3)
public abstract class AbstractFileResource extends AbstractVirtualFileResource {

    @Override
    protected final String getName() {
        return "file-resource.tmp";
    }
}
