package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ValueDescriptor;
import com.buschmais.jqassistant.plugin.java.annotation.jQASuppress;
import com.buschmais.jqassistant.plugin.java.api.model.*;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.java.impl.scanner.ClassFileScannerConfiguration;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics.TypeVariableResolver;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Class containing helper methods for ASM visitors.
 */
public class ClassFileVisitorContext {

    public static final int ASM_OPCODES = Opcodes.ASM9;

    /**
     * The name of constructor methods.
     */
    private static final String CONSTRUCTOR_METHOD = "void <init>";

    private final ClassFileDescriptor classFileDescriptor;

    @Getter
    private final ScannerContext scannerContext;

    @Getter
    private final ClassFileScannerConfiguration configuration;

    @Getter
    private final TypeVariableResolver typeVariableResolver;

    private final Map<TypeDescriptor, Integer> dependencyCache = new HashMap<>();

    private final Map<TypeDescriptor, Map<String, MemberDescriptor>> memberCache = new HashMap<>();

    /**
     * Constructor.
     *
     * @param scannerContext
     *     The scanner context
     * @param configuration
     *     The configuration.
     */
    public ClassFileVisitorContext(ClassFileDescriptor classFileDescriptor, ScannerContext scannerContext, ClassFileScannerConfiguration configuration) {
        this.classFileDescriptor = classFileDescriptor;
        this.scannerContext = scannerContext;
        this.configuration = configuration;
        this.typeVariableResolver = new TypeVariableResolver();
    }

    public Store getStore() {
        return scannerContext.getStore();
    }

    /*
     * Return the type descriptor for the given type name.
     *
     * @param typeName The full qualified name of the type (e.g. java.lang.Object).
     * @param dependentType The containing type which depends on the resolved type.
     * @return The resolved CachedType.
     */
    public TypeDescriptor resolveType(String fullQualifiedName) {
        TypeDescriptor dependency = getTypeResolver().resolve(fullQualifiedName, scannerContext);
        if (!classFileDescriptor.equals(dependency)) {
            dependencyCache.compute(dependency, (typeDescriptor, integer) -> integer == null ? 1 : integer + 1);
        }
        return dependency;
    }

    /*
     * Return the type descriptor for the given type name.
     *
     * @param typeName The full qualified name of the type (e.g. java.lang.Object).
     *
     * @param type The expected type.
     */
    <T extends TypeClassFileDescriptor> T createType(String fullQualifiedName, FileDescriptor fileDescriptor, Class<T> descriptorType) {
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
     * @param typeDescriptor
     *     The containing type.
     * @param signature
     *     The method signature.
     * @return The method descriptor.
     */
    MethodDescriptor getMethodDescriptor(TypeDescriptor typeDescriptor, String signature) {
        Map<String, MemberDescriptor> membersPerOfType = getMembersOfType(typeDescriptor);
        return (MethodDescriptor) membersPerOfType.computeIfAbsent(signature, s -> {
            MethodDescriptor methodDescriptor;
            if (s.startsWith(CONSTRUCTOR_METHOD)) {
                methodDescriptor = scannerContext.getStore()
                    .create(ConstructorDescriptor.class);
            } else {
                methodDescriptor = scannerContext.getStore()
                    .create(MethodDescriptor.class);
            }
            methodDescriptor.setSignature(s);
            typeDescriptor.getDeclaredMethods()
                .add(methodDescriptor);
            return methodDescriptor;
        });
    }

    /**
     * Return the field descriptor for the given type and field signature.
     *
     * @param typeDescriptor
     *     The containing type.
     * @param signature
     *     The field signature.
     * @return The field descriptor.
     */
    FieldDescriptor getFieldDescriptor(TypeDescriptor typeDescriptor, String signature) {
        Map<String, MemberDescriptor> membersOfType = getMembersOfType(typeDescriptor);
        return (FieldDescriptor) membersOfType.computeIfAbsent(signature, s -> {
            FieldDescriptor fieldDescriptor = scannerContext.getStore()
                .create(FieldDescriptor.class);
            fieldDescriptor.setSignature(s);
            typeDescriptor.getDeclaredFields()
                .add(fieldDescriptor);
            return fieldDescriptor;
        });
    }

    private @NonNull Map<String, MemberDescriptor> getMembersOfType(TypeDescriptor typeDescriptor) {
        return memberCache.computeIfAbsent(typeDescriptor, t -> {
            Map<String, MemberDescriptor> members = new HashMap<>();
            for (MemberDescriptor memberDescriptor : typeDescriptor.getDeclaredMembers()) {
                members.put(memberDescriptor.getSignature(), memberDescriptor);
            }
            return members;
        });
    }

    public ParameterDescriptor addParameterDescriptor(MethodDescriptor methodDescriptor, int index) {
        ParameterDescriptor parameterDescriptor = scannerContext.getStore()
            .create(ParameterDescriptor.class);
        parameterDescriptor.setIndex(index);
        methodDescriptor.getParameters()
            .add(parameterDescriptor);
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
     *     The invoking method.
     * @param lineNumber
     *     The line number.
     * @param invokedMethodDescriptor
     *     The invoked method.
     */
    void addInvokes(MethodDescriptor methodDescriptor, final Integer lineNumber, MethodDescriptor invokedMethodDescriptor) {
        InvokesDescriptor invokesDescriptor = scannerContext.getStore()
            .create(methodDescriptor, InvokesDescriptor.class, invokedMethodDescriptor);
        invokesDescriptor.setLineNumber(lineNumber);
    }

    /**
     * Add a reads relation between a method and a field.
     *
     * @param methodDescriptor
     *     The method.
     * @param lineNumber
     *     The line number.
     * @param fieldDescriptor
     *     The field.
     */
    void addReads(MethodDescriptor methodDescriptor, final Integer lineNumber, FieldDescriptor fieldDescriptor) {
        ReadsDescriptor readsDescriptor = scannerContext.getStore()
            .create(methodDescriptor, ReadsDescriptor.class, fieldDescriptor);
        readsDescriptor.setLineNumber(lineNumber);
    }

    /**
     * Add a writes relation between a method and a field.
     *
     * @param methodDescriptor
     *     The method.
     * @param lineNumber
     *     The line number.
     * @param fieldDescriptor
     *     The field.
     */
    void addWrites(MethodDescriptor methodDescriptor, final Integer lineNumber, FieldDescriptor fieldDescriptor) {
        WritesDescriptor writesDescriptor = scannerContext.getStore()
            .create(methodDescriptor, WritesDescriptor.class, fieldDescriptor);
        writesDescriptor.setLineNumber(lineNumber);
    }

    /**
     * Return the field descriptor for the given type and field signature.
     *
     * @param name
     *     The variable name.
     * @param signature
     *     The variable signature.
     * @return The field descriptor.
     */
    VariableDescriptor getVariableDescriptor(String name, String signature) {
        VariableDescriptor variableDescriptor = scannerContext.getStore()
            .create(VariableDescriptor.class);
        variableDescriptor.setName(name);
        variableDescriptor.setSignature(signature);
        return variableDescriptor;
    }

    /**
     * Creates a {@link ValueDescriptor}.
     *
     * @param valueDescriptorType
     *     The type of the value descriptor.
     * @param <T>
     *     The type of the value descriptor.
     * @return The value descriptor.
     */
    <T extends ValueDescriptor<?>> T getValueDescriptor(Class<T> valueDescriptorType) {
        return scannerContext.getStore()
            .create(valueDescriptorType);
    }

    /**
     * Add an annotation descriptor of the given type name to an annotated
     * descriptor.
     *
     * @param annotatedDescriptor
     *     The annotated descriptor.
     * @param typeName
     *     The type name of the annotation.
     * @return The annotation descriptor.
     */
    AnnotationVisitor addAnnotation(AnnotatedDescriptor annotatedDescriptor, String typeName) {
        if (typeName == null) {
            return null;
        }
        if (jQASuppress.class.getName()
            .equals(typeName) || com.buschmais.jqassistant.plugin.java.api.jQASuppress.class.getName()
            .equals(typeName)) {
            return new SuppressAnnotationVisitor(annotatedDescriptor, this);
        }
        TypeDescriptor type = resolveType(typeName);
        AnnotationValueDescriptor annotationDescriptor = scannerContext.getStore()
            .create(AnnotationValueDescriptor.class);
        annotationDescriptor.setType(type);
        annotatedDescriptor.getAnnotatedBy()
            .add(annotationDescriptor);
        return new AnnotationValueVisitor(annotationDescriptor, this);
    }

    public void flush() {
        for (Map.Entry<TypeDescriptor, Integer> entry : dependencyCache.entrySet()) {
            TypeDescriptor dependency = entry.getKey();
            final Integer weight = entry.getValue();
            ClassFileDependsOnDescriptor dependsOnDescriptor = scannerContext.getStore()
                .create(classFileDescriptor, ClassFileDependsOnDescriptor.class, dependency);
            dependsOnDescriptor.setWeight(weight);
        }
    }

    /**
     * Checks whether the value contains the flag.
     *
     * @param value
     *     the value
     * @param flag
     *     the flag
     * @return <code>true</code> if (value & flag) == flag, otherwise
     * <code>false</code>.
     */
    public boolean hasFlag(int value, int flag) {
        return (value & flag) == flag;
    }
}
