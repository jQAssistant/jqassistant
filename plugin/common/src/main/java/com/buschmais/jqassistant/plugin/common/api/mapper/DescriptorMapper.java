package com.buschmais.jqassistant.plugin.common.api.mapper;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

import org.mapstruct.Context;
import org.mapstruct.ObjectFactory;
import org.mapstruct.TargetType;

/**
 * Base interface for mapping objects to XO {@link Descriptor}s.
 *
 * @param <V>
 *     The value type.
 * @param <D>
 *     The {@link Descriptor} type.
 */

public interface DescriptorMapper<V, D extends Descriptor> {

    /**
     * Map a object to a {@link Descriptor}.
     *
     * @param value
     *     The object.
     * @param scanner
     *     The {@link Scanner}.
     * @return The mapped {@link Descriptor}.
     */
    D toDescriptor(V value, @Context Scanner scanner);

    /**
     * Factory method for {@link Descriptor}s.
     *
     * @param value
     *     The object the {@link Descriptor} is resolved for.
     * @param descriptorType
     *     The type of the {@link Descriptor} to be resolved.
     * @param scanner
     *     The {@link Scanner}.
     * @return The resolved {@link Descriptor}.
     */
    @ObjectFactory
    default D resolve(V value, @TargetType Class<D> descriptorType, @Context Scanner scanner) {
        return scanner.getContext()
            .getStore()
            .create(descriptorType);
    }

}
