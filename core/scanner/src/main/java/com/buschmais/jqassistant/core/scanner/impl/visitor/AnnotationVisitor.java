package com.buschmais.jqassistant.core.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.FieldDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.ValueDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.value.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

/**
 * An annotation visitor.
 * <p>Adds a dependency from the annotated types to the types of the annotation values.</p>
 */
public class AnnotationVisitor extends org.objectweb.asm.AnnotationVisitor {

    private VisitorHelper visitorHelper;

    private ArrayValueDescriptor arrayValueDescriptor;

    private AnnotationValueDescriptor annotationValueDescriptor;

    /**
     * Constructor.
     *
     * @param visitorHelper The {@link VisitorHelper}.
     */
    protected AnnotationVisitor(AnnotationValueDescriptor annotationValueDescriptor, VisitorHelper visitorHelper) {
        super(Opcodes.ASM4);
        this.annotationValueDescriptor = annotationValueDescriptor;
        this.visitorHelper = visitorHelper;
    }

    @Override
    public void visit(final String name, final Object value) {
        if (value instanceof Type) {
            String type = visitorHelper.getType((Type) value);
            ClassValueDescriptor valueDescriptor = createValue(ClassValueDescriptor.class, name);
            valueDescriptor.setValue(visitorHelper.getTypeDescriptor(type));
            addValue(name, valueDescriptor);
        } else {
            PrimitiveValueDescriptor valueDescriptor = createValue(PrimitiveValueDescriptor.class, name);
            TypeDescriptor typeDescriptor = visitorHelper.getTypeDescriptor(value.getClass().getName());
            valueDescriptor.setType(typeDescriptor);
            valueDescriptor.setValue(value);
            addValue(name, valueDescriptor);
        }
    }

    @Override
    public void visitEnum(final String name, final String desc, final String value) {
        EnumerationValueDescriptor valueDescriptor = createValue(EnumerationValueDescriptor.class, name);
        TypeDescriptor typeDescriptor = visitorHelper.getTypeDescriptor(visitorHelper.getType(desc));
        FieldDescriptor fieldDescriptor = visitorHelper.getFieldDescriptor(typeDescriptor, value);
        valueDescriptor.setType(visitorHelper.getTypeDescriptor(Enum.class.getName()));
        valueDescriptor.setValue(fieldDescriptor);
        addValue(name, valueDescriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        AnnotationValueDescriptor valueDescriptor = createValue(AnnotationValueDescriptor.class, name);
        valueDescriptor.setType(visitorHelper.getTypeDescriptor(Annotation.class.getName()));
        addValue(name, valueDescriptor);
        return new AnnotationVisitor(valueDescriptor, visitorHelper);
    }

    @Override
    public AnnotationVisitor visitArray(final String name) {
        this.arrayValueDescriptor = createValue(ArrayValueDescriptor.class, name);
        addValue(name, arrayValueDescriptor);
        return this;
    }

    @Override
    public void visitEnd() {
    }

    /**
     * Create a value descriptor of given type and name and initializes it.
     *
     * @param type The class type.
     * @param name The name
     * @param <T>  The type.
     * @return The initialized descriptor.
     */
    private <T extends ValueDescriptor> T createValue(Class<T> type, String name) {
        if (name != null) {
            this.arrayValueDescriptor = null;
        }
        String valueName;
        String fullQualifiedName;
        if (arrayValueDescriptor != null) {
            valueName = "[" + getListValue(this.arrayValueDescriptor).size() + "]";
            fullQualifiedName = this.arrayValueDescriptor.getFullQualifiedName() + valueName;
        } else {
            valueName = name;
            fullQualifiedName = this.annotationValueDescriptor.getFullQualifiedName() + ":" + valueName;
        }
        T valueDescriptor = visitorHelper.getValueDescriptor(type, fullQualifiedName);
        valueDescriptor.setName(valueName);
        return valueDescriptor;
    }

    /**
     * Add the descriptor as value to the current annotation or array value.
     *
     * @param name  The name.
     * @param value The value.
     */
    private void addValue(String name, ValueDescriptor value) {
        if (arrayValueDescriptor != null && name == null) {
            getListValue(arrayValueDescriptor).add(value);
        } else {
            getListValue(annotationValueDescriptor).add(value);
        }
    }

    /**
     * Get the list of referenced values.
     *
     * @param valueDescriptor The value descriptor containing a list value.
     * @param <T>             The type of the value descriptor.
     * @return The list of referenced values.
     */
    private <T extends ValueDescriptor<List<ValueDescriptor>>> List<ValueDescriptor> getListValue(T valueDescriptor) {
        List<ValueDescriptor> values = valueDescriptor.getValue();
        if (values == null) {
            values = new LinkedList<>();
            valueDescriptor.setValue(values);
        }
        return values;
    }
}
