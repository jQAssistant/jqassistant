package com.buschmais.jqassistant.core.test.matcher;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;

public class AbstractDescriptorMatcher<T extends FullQualifiedNameDescriptor>
    extends com.buschmais.jqassistant.core.store.test.matcher.AbstractDescriptorMatcher<T> {

    /**
     * Constructor.
     *
     * @param type
     *     The descriptor types.
     * @param fullQualifiedName
     *     The fully qualified name.
     */
    protected AbstractDescriptorMatcher(Class<T> type, String fullQualifiedName) {
        super(type, fullQualifiedName);
    }
}
