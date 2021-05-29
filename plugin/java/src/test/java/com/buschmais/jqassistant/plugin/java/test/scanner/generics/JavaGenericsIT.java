package com.buschmais.jqassistant.plugin.java.test.scanner.generics;

import java.lang.reflect.*;
import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.model.generics.TypeVariableDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.*;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class JavaGenericsIT extends AbstractJavaPluginIT {

    @Test
    void unboundClassTypeParameters() {
        scanClasses(UnboundClassTypeParameters.class);
        store.beginTransaction();
        List<TypeVariableDescriptor> typeParameters = query(
                "MATCH (:Type:GenericDeclaration{name:'UnboundClassTypeParameters'})-[declares:DECLARES_TYPE_PARAMETER]->(typeParameter:Java:ByteCode:Bound:TypeVariable) "
                        + //
                        "RETURN typeParameter ORDER BY declares.index").getColumn("typeParameter");
        assertThat(typeParameters).hasSize(2);
        assertThat(typeParameters.get(0)).matches(x -> x.getName().equals("X"));
        assertThat(typeParameters.get(1)).matches(y -> y.getName().equals("Y"));
        store.commitTransaction();
    }

    @Test
    void boundClassTypeParameters() {
        evaluate("typeParameters", ((Class<?>) BoundClassTypeParameter.class).getTypeParameters(), 0);
        scanClasses(BoundClassTypeParameter.class);
    }

    @Test
    void implementsGeneric() {
        evaluate("genericInterfaces", ImplementsGeneric.class.getGenericInterfaces(), 0);
        scanClasses(ImplementsGeneric.class);
    }

    @Test
    void extendsGeneric() {
        evaluate("genericSuperClass", ExtendsGeneric.class.getGenericSuperclass(), 0);
        scanClasses(ExtendsGeneric.class);
    }

    @Test
    void genericFieldWithParameterizedType() {
        scanClasses(GenericField.class);
    }

    @Test
    void genericMethodWithParameterizedType() {
        scanClasses(GenericMethod.class);
    }

    @Test
    void innerClass() {
        scanClasses(InnerClass.Inner.class);
    }

    private void evaluate(String prefix, Type[] types, int level) {
        evaluate(prefix, asList(types), level);
    }

    private void evaluate(String prefix, Iterable<? extends Type> types, int level) {
        for (Type type : types) {
            evaluate(prefix, type, level);
        }
    }

    private void evaluate(String prefix, Type type, int level) {
        int effectiveLevel = level + 1;
        for (int i = 0; i < effectiveLevel * 2; i++) {
            System.out.print(' ');
        }
        System.out.println(prefix + ": " + type + " (" + type.getClass() + ")");
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            evaluate("actualTypeArgument", parameterizedType.getActualTypeArguments(), effectiveLevel);
        } else if (type instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;
            evaluate("bound", typeVariable.getBounds(), effectiveLevel);
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            evaluate("lowerBound", wildcardType.getLowerBounds(), effectiveLevel);
            evaluate("upperBound", wildcardType.getUpperBounds(), effectiveLevel);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            evaluate("genericComponentType", genericArrayType.getGenericComponentType(), effectiveLevel);
        }
    }
}
