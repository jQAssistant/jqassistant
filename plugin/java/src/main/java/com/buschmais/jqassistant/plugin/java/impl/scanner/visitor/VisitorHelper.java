package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ValueDescriptor;
import com.buschmais.jqassistant.plugin.java.api.annotation.jQASuppress;
import com.buschmais.jqassistant.plugin.java.api.model.*;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.java.impl.scanner.ClassModelConfiguration;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Class containing helper methods for ASM visitors.
 */
public class VisitorHelper {

    public static final int ASM_OPCODES= Opcodes.ASM7;

    /**
     * The name of constructor methods.
     */
    private static final String CONSTRUCTOR_METHOD = "void <init>";

    private ScannerContext scannerContext;

    private ClassModelConfiguration classModelConfiguration;

    /**
     * Constructor.
     *
     * @param scannerContext
     *            The scanner context
     * @param classModelConfiguration
     */
    public VisitorHelper(ScannerContext scannerContext, ClassModelConfiguration classModelConfiguration) {
        this.scannerContext = scannerContext;
        this.classModelConfiguration = classModelConfiguration;
    }

    ClassModelConfiguration getClassModelConfiguration() {
        return classModelConfiguration;
    }

    /*
     * Return the type descriptor for the given type name.
     *
     * @param typeName The full qualified name of the type (e.g. java.lang.Object).
     */
    TypeCache.CachedType resolveType(String fullQualifiedName, TypeCache.CachedType<? extends ClassFileDescriptor> dependentType) {
        TypeCache.CachedType cachedType = getTypeResolver().resolve(fullQualifiedName, scannerContext);
        if (!dependentType.equals(cachedType)) {
            dependentType.addDependency(cachedType.getTypeDescriptor());
        }
        return cachedType;
    }

    /*
     * Return the type descriptor for the given type name.
     *
     * @param typeName The full qualified name of the type (e.g. java.lang.Object).
     *
     * @param type The expected type.
     */
    <T extends ClassFileDescriptor> TypeCache.CachedType<T> createType(String fullQualifiedName, FileDescriptor fileDescriptor, Class<T> descriptorType) {
        return getTypeResolver().create(fullQualifiedName, fileDescriptor, descriptorType, scannerContext);
    }

    /**
     * Return the type resolver.
     * <p>
     * Looks up an instance in the scanner context. If none can be found the default
     * resolver is used.
     * </p>
     *
     * @return The type resolver.
     */
    private TypeResolver getTypeResolver() {
        TypeResolver typeResolver = scannerContext.peek(TypeResolver.class);
        if (typeResolver == null) {
            throw new IllegalStateException("Cannot find Java type resolver.");
        }
        return typeResolver;
    }

    /**
     * Return the method descriptor for the given type and method signature.
     *
     * @param cachedType
     *            The containing type.
     * @param signature
     *            The method signature.
     * @return The method descriptor.
     */
    MethodDescriptor getMethodDescriptor(TypeCache.CachedType<?> cachedType, String signature) {
        MethodDescriptor methodDescriptor = cachedType.getMethod(signature);
        if (methodDescriptor == null) {
            if (signature.startsWith(CONSTRUCTOR_METHOD)) {
                methodDescriptor = scannerContext.getStore().create(ConstructorDescriptor.class);
            } else {
                methodDescriptor = scannerContext.getStore().create(MethodDescriptor.class);
            }
            methodDescriptor.setSignature(signature);
            cachedType.addMember(signature, methodDescriptor);
        }
        return methodDescriptor;
    }

    ParameterDescriptor addParameterDescriptor(MethodDescriptor methodDescriptor, int index) {
        ParameterDescriptor parameterDescriptor = scannerContext.getStore().create(ParameterDescriptor.class);
        parameterDescriptor.setIndex(index);
        methodDescriptor.getParameters().add(parameterDescriptor);
        return parameterDescriptor;
    }

    ParameterDescriptor getParameterDescriptor(MethodDescriptor methodDescriptor, int index) {
        for (ParameterDescriptor parameterDescriptor : methodDescriptor.getParameters()) {
            if (parameterDescriptor.getIndex() == index) {
                return parameterDescriptor;
            }
        }
        return null;
    }

    /**
     * Add a invokes relation between two methods.
     *
     * @param methodDescriptor
     *            The invoking method.
     * @param lineNumber
     *            The line number.
     * @param invokedMethodDescriptor
     *            The invoked method.
     */
    void addInvokes(MethodDescriptor methodDescriptor, final Integer lineNumber, MethodDescriptor invokedMethodDescriptor) {
        InvokesDescriptor invokesDescriptor = scannerContext.getStore().create(methodDescriptor, InvokesDescriptor.class, invokedMethodDescriptor);
        invokesDescriptor.setLineNumber(lineNumber);
    }

    /**
     * Add a reads relation between a method and a field.
     *
     * @param methodDescriptor
     *            The method.
     * @param lineNumber
     *            The line number.
     * @param fieldDescriptor
     *            The field.
     */
    void addReads(MethodDescriptor methodDescriptor, final Integer lineNumber, FieldDescriptor fieldDescriptor) {
        ReadsDescriptor readsDescriptor = scannerContext.getStore().create(methodDescriptor, ReadsDescriptor.class, fieldDescriptor);
        readsDescriptor.setLineNumber(lineNumber);
    }

    /**
     * Add a writes relation between a method and a field.
     *
     * @param methodDescriptor
     *            The method.
     * @param lineNumber
     *            The line number.
     * @param fieldDescriptor
     *            The field.
     */
    void addWrites(MethodDescriptor methodDescriptor, final Integer lineNumber, FieldDescriptor fieldDescriptor) {
        WritesDescriptor writesDescriptor = scannerContext.getStore().create(methodDescriptor, WritesDescriptor.class, fieldDescriptor);
        writesDescriptor.setLineNumber(lineNumber);
    }

    /**
     * Return the field descriptor for the given type and field signature.
     *
     * @param cachedType
     *            The containing type.
     * @param signature
     *            The field signature.
     * @return The field descriptor.
     */
    FieldDescriptor getFieldDescriptor(TypeCache.CachedType<?> cachedType, String signature) {
        FieldDescriptor fieldDescriptor = cachedType.getField(signature);
        if (fieldDescriptor == null) {
            fieldDescriptor = scannerContext.getStore().create(FieldDescriptor.class);
            fieldDescriptor.setSignature(signature);
            cachedType.addMember(signature, fieldDescriptor);
        }
        return fieldDescriptor;
    }

    /**
     * Return the field descriptor for the given type and field signature.
     *
     * @param name
     *            The variable name.
     * @param signature
     *            The variable signature.
     * @return The field descriptor.
     */
    VariableDescriptor getVariableDescriptor(String name, String signature) {
        VariableDescriptor variableDescriptor = scannerContext.getStore().create(VariableDescriptor.class);
        variableDescriptor.setName(name);
        variableDescriptor.setSignature(signature);
        return variableDescriptor;
    }

    /**
     * Creates a {@link ValueDescriptor}.
     *
     * @param valueDescriptorType
     *            The type of the value descriptor.
     * @param <T>
     *            The type of the value descriptor.
     * @return The value descriptor.
     */
    <T extends ValueDescriptor<?>> T getValueDescriptor(Class<T> valueDescriptorType) {
        return scannerContext.getStore().create(valueDescriptorType);
    }

    /**
     * Add an annotation descriptor of the given type name to an annotated
     * descriptor.
     *
     * @param annotatedDescriptor
     *            The annotated descriptor.
     * @param typeName
     *            The type name of the annotation.
     * @return The annotation descriptor.
     */
    AnnotationVisitor addAnnotation(TypeCache.CachedType containingDescriptor, AnnotatedDescriptor annotatedDescriptor, String typeName) {
        if (typeName == null) {
            return null;
        }
        if (jQASuppress.class.getName().equals(typeName)) {
            JavaSuppressDescriptor javaSuppressDescriptor = scannerContext.getStore().addDescriptorType(annotatedDescriptor, JavaSuppressDescriptor.class);
            return new SuppressAnnotationVisitor(javaSuppressDescriptor);
        }
        TypeDescriptor type = resolveType(typeName, containingDescriptor).getTypeDescriptor();
        AnnotationValueDescriptor annotationDescriptor = scannerContext.getStore().create(AnnotationValueDescriptor.class);
        annotationDescriptor.setType(type);
        annotatedDescriptor.getAnnotatedBy().add(annotationDescriptor);
        return new AnnotationValueVisitor(containingDescriptor, annotationDescriptor, this);
    }

    public void storeDependencies(TypeCache.CachedType<?> cachedType) {
        Map<TypeDescriptor, Integer> dependencies = cachedType.getDependencies();
        for (Map.Entry<TypeDescriptor, Integer> entry : dependencies.entrySet()) {
            TypeDescriptor dependency = entry.getKey();
            final Integer weight = entry.getValue();
            TypeDescriptor dependent = cachedType.getTypeDescriptor();
            TypeDependsOnDescriptor dependsOnDescriptor = scannerContext.getStore().create(dependent, TypeDependsOnDescriptor.class, dependency);
            if (classModelConfiguration.isTypeDependsOnWeight()) {
                dependsOnDescriptor.setWeight(weight);
            }
        }
    }
}
