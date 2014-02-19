package com.buschmais.jqassistant.plugin.java.impl.store.visitor;

import com.buschmais.jqassistant.plugin.java.api.SignatureHelper;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractAnnotationVisitor<D> extends org.objectweb.asm.AnnotationVisitor {

    private VisitorHelper visitorHelper;

    private ArrayValueDescriptor arrayValueDescriptor;

    private D descriptor;

    /**
     * Constructor.
     *
     * @param visitorHelper The {@link VisitorHelper}.
     */
    protected AbstractAnnotationVisitor(D descriptor, VisitorHelper visitorHelper) {
        super(Opcodes.ASM4);
        this.descriptor = descriptor;
        this.visitorHelper = visitorHelper;
    }

    @Override
    public void visit(final String name, final Object value) {
        if (value instanceof Type) {
            String type = SignatureHelper.getType((Type) value);
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
        EnumValueDescriptor valueDescriptor = createValue(EnumValueDescriptor.class, name);
        TypeDescriptor typeDescriptor = visitorHelper.getTypeDescriptor(SignatureHelper.getType(desc));
        FieldDescriptor fieldDescriptor = visitorHelper.getFieldDescriptor(typeDescriptor, SignatureHelper.getFieldSignature(value, desc));
        valueDescriptor.setType(visitorHelper.getTypeDescriptor(Enum.class.getName()));
        valueDescriptor.setValue(fieldDescriptor);
        addValue(name, valueDescriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        AnnotationValueDescriptor valueDescriptor = createValue(AnnotationValueDescriptor.class, name);
        valueDescriptor.setType(visitorHelper.getTypeDescriptor(SignatureHelper.getType(desc)));
        addValue(name, valueDescriptor);
        return new AnnotationVisitor(valueDescriptor, visitorHelper);
    }

    @Override
    public org.objectweb.asm.AnnotationVisitor visitArray(final String name) {
        this.arrayValueDescriptor = createValue(ArrayValueDescriptor.class, name);
        setValue(descriptor, arrayValueDescriptor);
        return this;
    }

    @Override
    public void visitEnd() {
    }

    protected abstract void setValue(D descriptor, ValueDescriptor value);

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
        if (arrayValueDescriptor != null) {
            valueName = "[" + getListValue(this.arrayValueDescriptor).size() + "]";
        } else {
            valueName = name;
        }
        T valueDescriptor = visitorHelper.getValueDescriptor(type);
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
            setValue(descriptor, value);
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
