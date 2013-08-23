package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypedValueDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.ValueDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.value.*;
import com.buschmais.jqassistant.core.store.api.model.NodeLabel;
import com.buschmais.jqassistant.core.store.api.model.NodeProperty;
import com.buschmais.jqassistant.core.store.api.model.Relation;
import com.buschmais.jqassistant.core.store.api.model.ValueLabel;
import org.neo4j.graphdb.Label;

import java.util.*;

import static com.buschmais.jqassistant.core.store.api.model.NodeLabel.VALUE;

/**
 * A mapper for {@link com.buschmais.jqassistant.core.model.api.descriptor.ValueDescriptor}s.
 */
public class ValueDescriptorMapper extends AbstractDescriptorMapper<ValueDescriptor> {

    @Override
    public Set<Class<? extends ValueDescriptor>> getJavaType() {
        Set<Class<? extends ValueDescriptor>> javaTypes = new HashSet<>();
        javaTypes.add(AnnotationValueDescriptor.class);
        javaTypes.add(ArrayValueDescriptor.class);
        javaTypes.add(EnumerationValueDescriptor.class);
        javaTypes.add(PrimitiveValueDescriptor.class);
        javaTypes.add(ClassValueDescriptor.class);
        return javaTypes;
    }

    @Override
    public NodeLabel getCoreLabel() {
        return VALUE;
    }

    @Override
    public ValueDescriptor createInstance(Set<Label> labels) {
        for (Label label : labels) {
            ValueLabel valueLabel = ValueLabel.getValueLabel(label.name());
            if (valueLabel != null) {
                switch (valueLabel) {
                    case ARRAY:
                        return new ArrayValueDescriptor();
                    case ANNOTATION:
                        return new AnnotationValueDescriptor();
                    case ENUM:
                        return new EnumerationValueDescriptor();
                    case PRIMITIVE:
                        return new PrimitiveValueDescriptor();
                    case CLASS:
                        return new ClassValueDescriptor();
                    default:
                        throw new IllegalStateException("Unsupported value label " + valueLabel);
                }
            }
        }
        throw new IllegalStateException("Cannot find a valid value label in " + labels);
    }

    @Override
    public ValueDescriptor createInstance(Class<? extends ValueDescriptor> type) {
        if (ArrayValueDescriptor.class.equals(type)) {
            return new ArrayValueDescriptor();
        } else if (AnnotationValueDescriptor.class.equals(type)) {
            return new AnnotationValueDescriptor();
        } else if (EnumerationValueDescriptor.class.equals(type)) {
            return new EnumerationValueDescriptor();
        } else if (PrimitiveValueDescriptor.class.equals(type)) {
            return new PrimitiveValueDescriptor();
        } else if (ClassValueDescriptor.class.equals(type)) {
            return new ClassValueDescriptor();
        }
        throw new IllegalStateException("Unsupported value type " + type.getName());
    }

    @Override
    public Map<Relation, Set<? extends Descriptor>> getRelations(ValueDescriptor descriptor) {
        Map<Relation, Set<? extends Descriptor>> relations = new HashMap<>();
        Object value = descriptor.getValue();
        if (value != null) {
            Set<? extends Descriptor> values;
            if (descriptor instanceof ArrayValueDescriptor) {
                values = new HashSet<>((List<? extends ValueDescriptor>) value);
            } else if (descriptor instanceof AnnotationValueDescriptor) {
                values = new HashSet<>((List<? extends ValueDescriptor>) value);
            } else if (!(descriptor instanceof PrimitiveValueDescriptor)) {
                values = asSet((Descriptor) value);
            } else {
                values = Collections.emptySet();
            }
            relations.put(Relation.HAS, values);
        }
        if (descriptor instanceof TypedValueDescriptor) {
            relations.put(Relation.OF_TYPE, asSet(((TypedValueDescriptor) descriptor).getType()));
        }
        return relations;
    }

    @Override
    protected void setRelation(ValueDescriptor descriptor, Relation relation, Descriptor target) {
        switch (relation) {
            case HAS:
                if (descriptor instanceof ArrayValueDescriptor) {
                    ArrayValueDescriptor arrayValueDescriptor = (ArrayValueDescriptor) descriptor;
                    List<ValueDescriptor> values = arrayValueDescriptor.getValue();
                    if (values == null) {
                        values = new LinkedList<>();
                        arrayValueDescriptor.setValue(values);
                    }
                    values.add((ValueDescriptor) target);
                } else if (descriptor instanceof AnnotationValueDescriptor) {
                    AnnotationValueDescriptor annotationValueDescriptor = (AnnotationValueDescriptor) descriptor;
                    List<ValueDescriptor> values = annotationValueDescriptor.getValue();
                    if (values == null) {
                        values = new LinkedList<>();
                        annotationValueDescriptor.setValue(values);
                    }
                    values.add((ValueDescriptor) target);
                } else if (!(descriptor instanceof PrimitiveValueDescriptor)) {
                    descriptor.setValue(target);
                }
                break;
            case OF_TYPE:
                if (descriptor instanceof TypedValueDescriptor) {
                    ((TypedValueDescriptor) descriptor).setType((TypeDescriptor) target);
                }
                break;
            default:
        }
    }

    @Override
    public Map<NodeProperty, Object> getProperties(ValueDescriptor descriptor) {
        Map<NodeProperty, Object> properties = super.getProperties(descriptor);
        properties.put(NodeProperty.NAME, descriptor.getName());
        Object value = descriptor.getValue();
        if (descriptor instanceof PrimitiveValueDescriptor) {
            properties.put(NodeProperty.VALUE, value);
        }
        return properties;
    }

    @Override
    public void setProperty(ValueDescriptor descriptor, NodeProperty property, Object value) {
        super.setProperty(descriptor, property, value);
        switch (property) {
            case NAME:
                descriptor.setName((String) value);
                break;
            case VALUE:
                if (descriptor instanceof PrimitiveValueDescriptor) {
                    descriptor.setValue(value);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public Set<Label> getLabels(ValueDescriptor descriptor) {
        final ValueLabel valueLabel;
        if (ArrayValueDescriptor.class.equals(descriptor.getClass())) {
            valueLabel = ValueLabel.ARRAY;
        } else if (AnnotationValueDescriptor.class.equals(descriptor.getClass())) {
            valueLabel = ValueLabel.ANNOTATION;
        } else if (EnumerationValueDescriptor.class.equals(descriptor.getClass())) {
            valueLabel = ValueLabel.ENUM;
        } else if (PrimitiveValueDescriptor.class.equals(descriptor.getClass())) {
            valueLabel = ValueLabel.PRIMITIVE;
        } else if (ClassValueDescriptor.class.equals(descriptor.getClass())) {
            valueLabel = ValueLabel.CLASS;
        } else {
            throw new IllegalStateException("Unsupported value descriptor type " + descriptor.getClass().getName());
        }
        Set<Label> labels = new HashSet<>();
        labels.add(new com.buschmais.jqassistant.core.store.impl.dao.mapper.Label(valueLabel)); return labels;
    }

    @Override
    public void setLabel(ValueDescriptor descriptor, Label label) {
    }
}
