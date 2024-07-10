package com.buschmais.jqassistant.plugin.common.api.mapper;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

import org.mapstruct.Context;
import org.mapstruct.MappingTarget;

/**
 * Base interface for enriching existing XO {@link Descriptor}s.
 * <p>
 * Usually used after retrieving a {@link FileDescriptor} from the {@link ScannerContext} and adding a specific label to it.
 *
 * @param <V>
 *     The value type.
 * @param <D>
 *     The {@link Descriptor} type.
 */
public interface DescriptorEnricher<V, D extends Descriptor> {

    /**
     * Enrich an existing {@link Descriptor} with the given value.
     *
     * @param type
     *     The object.
     * @param scanner
     *     The {@link Scanner}.
     * @return The mapped {@link Descriptor}.
     */
    D toDescriptor(V type, @MappingTarget D target, @Context Scanner scanner);

}
