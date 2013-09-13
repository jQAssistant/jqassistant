package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import org.apache.commons.collections.map.HashedMap;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.util.*;

/**
 * Abstract base implementation of a {@link DescriptorMapper}.
 *
 * @param <T> The types.
 */
public abstract class AbstractDescriptorMapper<T extends Descriptor, P extends Enum, R extends Enum & RelationshipType> implements DescriptorMapper<T> {

    private Map<String, P> properties;

    private Map<String, R> relations;

    /**
     * Return the enumeration type defining the relations of the descriptor.
     *
     * @return The enumeration type.
     */
    protected abstract Class<R> getRelationKeys();

    /**
     * Return the enumeration type defining the properties of the descriptor.
     *
     * @return The enumeration type.
     */
    protected abstract Class<P> getPropertyKeys();

    /**
     * Constructor.
     */
    protected AbstractDescriptorMapper() {
        properties = new HashMap<>();
        for (P p : getPropertyKeys().getEnumConstants()) {
            properties.put(p.name(), p);
        }
        relations = new HashedMap();
        for (R r : getRelationKeys().getEnumConstants()) {
            relations.put(r.name(), r);
        }
    }

    @Override
    public final Iterable<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public final Iterable<? extends RelationshipType> getRelationshipTypes() {
        return relations.values();
    }

    /**
     * Converts values to a {@link Set} of values.
     * <p>
     * This is a convenience method.
     * </p>
     *
     * @param values The values.
     * @return The {@link Set} containing the values.
     */
    protected <T> Set<T> asSet(T... values) {
        Set<T> set = new HashSet<>();
        if (values != null) {
            for (T value : values) {
                set.add(value);
            }
        }
        return set;
    }

    /**
     * Get a single entry from a set.
     *
     * @param set The set
     * @param <X> The value type.
     * @return The value.
     */
    protected <X> X getSingleEntry(Collection<X> set) {
        if (set.size() > 1) {
            throw new IllegalArgumentException("Collection must contain exactly one entry.");
        }
        Iterator<X> iterator = set.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    @Override
    public Long getId(T descriptor) {
        return descriptor.getId();
    }

    @Override
    public void setId(T descriptor, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Cannot set id which is null for descriptor " + descriptor);
        }
        descriptor.setId(id);
    }

    @Override
    public final Set<? extends Descriptor> getRelation(T descriptor, RelationshipType relationshipType) {
        return getRelation(descriptor, relations.get(relationshipType.name()));
    }

    protected abstract Set<? extends Descriptor> getRelation(T descriptor, R relation);

    @Override
    public final void setRelation(T descriptor, RelationshipType relationshipType, Set<? extends Descriptor> targets) {
        setRelation(descriptor, relations.get(relationshipType.name()), targets);
    }

    protected abstract void setRelation(T descriptor, R relation, Set<? extends Descriptor> targets);

    @Override
    public final Object getProperty(T descriptor, String propertyName) {
        return getProperty(descriptor, properties.get(propertyName));
    }

    protected abstract Object getProperty(T descriptor, P property);

    @Override
    public final void setProperty(T descriptor, String propertyName, Object value) {
        setProperty(descriptor, properties.get(propertyName), value);
    }

    protected abstract void setProperty(T descriptor, P property, Object value);

    @Override
    public Set<? extends Label> getLabels(T descriptor) {
        return Collections.emptySet();
    }

    @Override
    public void setLabel(T descriptor, Label label) {
    }
}
