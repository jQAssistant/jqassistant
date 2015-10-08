package com.buschmais.jqassistant.plugin.common.api.scanner;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;

/**
 * Abstract base implementation for plugins handling file or directory
 * resources.
 * 
 * @param <I>
 *            The resource item type.
 * @param <D>
 *            The descriptor type representing the item type.
 */
public abstract class AbstractResourceScannerPlugin<I, D extends Descriptor> extends AbstractScannerPlugin<I, D> {

    @Override
    public Class<? extends I> getType() {
        return getTypeParameter(AbstractResourceScannerPlugin.class, 0);
    }

    @Override
    public Class<? extends D> getDescriptorType() {
        return getTypeParameter(AbstractResourceScannerPlugin.class, 1);
    }

    /**
     *
     * @param path
     * @return
     */
    protected String slashify(String path) {
        return path.replace('\\', '/');
    }

}
