package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.function.Supplier;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.shared.map.MapBuilder;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.URIDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.xo.api.Query;

import lombok.extern.slf4j.Slf4j;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * Abstract base class for URI scanners.
 *
 * @param <R>
 *     The resource type represented by the URI and to be passed to subsequent scanners .
 */
@Slf4j
public abstract class AbstractUriScannerPlugin<R> extends AbstractScannerPlugin<URI, URIDescriptor> {

    @Override
    public Class<URI> getType() {
        return URI.class;
    }

    @Override
    public Class<URIDescriptor> getDescriptorType() {
        return URIDescriptor.class;
    }

    @Override
    public final URIDescriptor scan(URI uri, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        return getResource(uri, context).map(resource -> {
                log.debug("Resolved URI '{}' to resource '{}'.", uri, resource);
                Descriptor descriptor = scanner.scan(resource, path, scope);
                if (descriptor != null) {
                    URIDescriptor uriDescriptor = context.getStore()
                        .addDescriptorType(descriptor, URIDescriptor.class);
                    uriDescriptor.setUri(uri.toString());
                    return uriDescriptor;
                }
                return null;
            })
            .orElse(null);
    }

    /**
     * Get the resource identified by the {@link URI}.
     *
     * @param uri
     *     The {@link URI}
     * @param context
     *     The {@link ScannerContext}.
     * @return The resource.
     * @throws IOException
     *     If the resource cannot be opened.
     */
    protected abstract Optional<R> getResource(URI uri, ScannerContext context) throws IOException;

    /**
     * Convenience method for sub-classes to resolve a {@link URI} resource uniquely, i.e. only provide a resource if there's no matching {@link URIDescriptor}  in the store.
     *
     * @param uri
     *     The {@link URI}.
     * @param resourceSupplier
     *     The resource {@link Supplier}. The {@link Supplier} is allowed to return a <code>null</code> value.
     * @param context
     *     The {@link ScannerContext}.
     * @return An {@link Optional} representing the URI resource to be scanned.
     */
    protected final Optional<R> resolve(URI uri, Supplier<R> resourceSupplier, ScannerContext context) {
        Query.Result<Query.Result.CompositeRowObject> result = context.getStore()
            .executeQuery("MATCH (uri:URI{uri:$uri}) RETURN uri", MapBuilder.<String, Object>builder()
                .entry("uri", uri.toString())
                .build());
        if (result.hasResult()) {
            log.debug("URI '{}' has already been scanned, skipping.", uri);
            return empty();
        }
        return ofNullable(resourceSupplier.get());
    }
}
