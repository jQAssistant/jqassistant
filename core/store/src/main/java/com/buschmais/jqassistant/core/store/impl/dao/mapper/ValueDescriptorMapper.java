package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypedValueDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.ValueDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.value.*;
import com.buschmais.jqassistant.core.store.api.model.PrimaryLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.util.*;

import static com.buschmais.jqassistant.core.store.impl.dao.mapper.Label.label;
import static com.buschmais.jqassistant.core.store.impl.dao.mapper.NodeLabel.VALUE;

/**
 * A store for {@link com.buschmais.jqassistant.core.model.api.descriptor.ValueDescriptor}s.
 */
public class ValueDescriptorMapper extends AbstractDescriptorMapper<ValueDescriptor, ValueDescriptorMapper.Property, ValueDescriptorMapper.Relation> {

    enum Property {
        NAME,
        VALUE;
    }

    enum Relation implements RelationshipType {
        HAS,
        VALUE,
        OF_TYPE;
    }

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
    public PrimaryLabel getPrimaryLabel() {
        return VALUE;
    }

    @Override
    protected Class<Property> getPropertyKeys() {
        return Property.class;
    }

    @Override
    protected Class<Relation> getRelationKeys() {
        return Relation.class;
    }

    @Override
    public Class<? extends ValueDescriptor> getType(Set<Label> labels) {
        for (Label label : labels) {
            ValueLabel valueLabel = ValueLabel.getValueLabel(label.name());
            if (valueLabel != null) {
                switch (valueLabel) {
                    case ARRAY:
                        return ArrayValueDescriptor.class;
                    case ANNOTATION:
                        return AnnotationValueDescriptor.class;
                    case ENUM:
                        return EnumerationValueDescriptor.class;
                    case PRIMITIVE:
                        return PrimitiveValueDescriptor.class;
                    case CLASS:
                        return ClassValueDescriptor.class;
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
    public Set<? extends Descriptor> getRelation(ValueDescriptor descriptor, Relation relation) {
        switch (relation) {
            case HAS:
                Object value = descriptor.getValue();
                if (value != null) {
                    if (descriptor instanceof ArrayValueDescriptor) {
                        return new HashSet<>((List<? extends ValueDescriptor>) value);
                    } else if (descriptor instanceof AnnotationValueDescriptor) {
                        return new HashSet<>((List<? extends ValueDescriptor>) value);
                    } else if (!(descriptor instanceof PrimitiveValueDescriptor)) {
                        return asSet((Descriptor) value);
                    } else {
                        return Collections.emptySet();
                    }
                }
                break;
            case OF_TYPE:
                if (descriptor instanceof TypedValueDescriptor) {
                    return asSet(((TypedValueDescriptor) descriptor).getType());
                }
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    protected void setRelation(ValueDescriptor descriptor, Relation relation, Set<? extends Descriptor> target) {
        switch (relation) {
            case HAS:
                if (descriptor instanceof ArrayValueDescriptor) {
                    ArrayValueDescriptor arrayValueDescriptor = (ArrayValueDescriptor) descriptor;
                    List<ValueDescriptor> values = arrayValueDescriptor.getValue();
                    if (values == null) {
                        values = new LinkedList<>();
                        arrayValueDescriptor.setValue(values);
                    }
                    values.addAll((Collection<? extends ValueDescriptor>) target);
                } else if (descriptor instanceof AnnotationValueDescriptor) {
                    AnnotationValueDescriptor annotationValueDescriptor = (AnnotationValueDescriptor) descriptor;
                    List<ValueDescriptor> values = annotationValueDescriptor.getValue();
                    if (values == null) {
                        values = new LinkedList<>();
                        annotationValueDescriptor.setValue(values);
                    }
                    values.addAll((Collection<? extends ValueDescriptor>) target);
                } else if (!(descriptor instanceof PrimitiveValueDescriptor)) {
                    descriptor.setValue(getSingleEntry(target));
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
    public Object getProperty(ValueDescriptor descriptor, Property property) {
        switch (property) {
            case NAME:
                return descriptor.getName();
            case VALUE:
                if (descriptor instanceof PrimitiveValueDescriptor) {
                    return descriptor.getValue();
                }
                break;
            default:
        }
        return null;
    }

    @Override
    public void setProperty(ValueDescriptor descriptor, Property property, Object value) {
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
        labels.add(label(valueLabel));
        return labels;
    }

    @Override
    public void setLabel(ValueDescriptor descriptor, Label label) {
    }
}
