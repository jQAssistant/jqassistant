package com.buschmais.jqassistant.plugin.java.test.scanner.generics;

import java.lang.reflect.*;
import java.util.AbstractList;
import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.HasActualTypeArgumentDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.ParameterizedTypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.TypeVariableDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.ExtendsGeneric;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericTypeDeclarations;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.ImplementsGeneric;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.HamcrestCondition.matching;

/**
 * Verifies scanning of Java generic types.
 */
@Slf4j
public class JavaGenericsIT extends AbstractJavaPluginIT {

    @Test
    void outerClassTypeParameters() {
        scanClasses(GenericTypeDeclarations.class);
        store.beginTransaction();
        List<TypeVariableDescriptor> typeParameters = query(
                "MATCH (:Type:GenericDeclaration{name:'GenericTypeDeclarations'})-[declares:DECLARES_TYPE_PARAMETER]->(typeParameter:Java:ByteCode:Bound:TypeVariable) "
                        + //
                        "RETURN typeParameter ORDER BY declares.index").getColumn("typeParameter");
        assertThat(typeParameters).hasSize(2);
        assertThat(typeParameters.get(0)).matches(x -> x.getName().equals("X"));
        assertThat(typeParameters.get(1)).matches(y -> y.getName().equals("Y"));
        store.commitTransaction();
    }

    @Test
    void implementsGeneric() {
        evaluate("genericInterfaces", ImplementsGeneric.class.getGenericInterfaces(), 0);
        scanClasses(ImplementsGeneric.class);
        store.beginTransaction();
        List<TypeDescriptor> interfaces = query("MATCH (:Type{name:'ImplementsGeneric'})-[:IMPLEMENTS]->(interface:Java:ByteCode:Type) RETURN interface")
                .getColumn("interface");
        assertThat(interfaces).hasSize(1);
        assertThat(interfaces.get(0)).is(matching(typeDescriptor(List.class)));
        List<ParameterizedTypeDescriptor> parameterizedTypes = query(
                "MATCH (:Type{name:'ImplementsGeneric'})-[:IMPLEMENTS_GENERIC]->(parameterizedType:Java:ByteCode:Bound:ParameterizedType) RETURN parameterizedType")
                        .getColumn("parameterizedType");
        assertThat(parameterizedTypes).hasSize(1);
        verifyParameterizedType(parameterizedTypes.get(0), List.class, String.class);
        store.commitTransaction();
    }

    @Test
    void extendsGeneric() {
        evaluate("genericSuperClass", ExtendsGeneric.class.getGenericSuperclass(), 0);
        scanClasses(ExtendsGeneric.class);
        store.beginTransaction();
        List<TypeDescriptor> superClasses = query("MATCH (:Type{name:'ExtendsGeneric'})-[:EXTENDS]->(superClass:Java:ByteCode:Type) RETURN superClass")
                .getColumn("superClass");
        assertThat(superClasses).hasSize(1);
        assertThat(superClasses.get(0)).is(matching(typeDescriptor(AbstractList.class)));
        List<ParameterizedTypeDescriptor> parameterizedTypes = query(
                "MATCH (:Type{name:'ExtendsGeneric'})-[:EXTENDS_GENERIC]->(parameterizedType:Java:ByteCode:Bound:ParameterizedType) RETURN parameterizedType")
                        .getColumn("parameterizedType");
        verifyParameterizedType(parameterizedTypes.get(0), AbstractList.class, String.class);
        store.commitTransaction();
    }

    private void verifyParameterizedType(ParameterizedTypeDescriptor parameterizedType, Class<?> rawType, Class<?> typeArgument) {
        assertThat(parameterizedType.getRawType()).is(matching(typeDescriptor(rawType)));
        List<HasActualTypeArgumentDescriptor> actualTypeArguments = parameterizedType.getActualTypeArguments();
        assertThat(actualTypeArguments).hasSize(1);
        HasActualTypeArgumentDescriptor hasActualTypeArgument = actualTypeArguments.get(0);
        assertThat(hasActualTypeArgument.getIndex()).isEqualTo(0);
        BoundDescriptor stringType = hasActualTypeArgument.getGenericType();
        assertThat(stringType.getRawType()).is(matching(typeDescriptor(typeArgument)));
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
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < effectiveLevel * 2; i++) {
            indent.append(' ');
        }
        log.info("{} {}: {} ({})", indent, prefix, type, type.getClass());
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
