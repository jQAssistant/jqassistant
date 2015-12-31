package com.buschmais.jqassistant.plugin.common.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

/**
 * Abstract base implementation for plugins handling file or directory
 * resources.
 * 
 * @param <I>
 *            The resource item type.
 * @param <D>
 *            The descriptor type representing the item type.
 * @param <P>
 *            The actuall plugin type.
 */
public abstract class AbstractResourceScannerPlugin<I, D extends Descriptor, P extends ScannerPlugin<I, D>>
        extends AbstractScannerPlugin<I, D, P> {

    @Override
    public Class<? extends I> getType() {
        return getTypeParameter(AbstractResourceScannerPlugin.class, 0);
    }

    @Override
    public Class<D> getDescriptorType() {
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
