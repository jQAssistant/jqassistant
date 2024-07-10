package com.buschmais.jqassistant.plugin.java.test.scanner.generics;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.AbstractList;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.shared.map.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.model.*;
import com.buschmais.jqassistant.plugin.java.api.model.generics.*;
import com.buschmais.jqassistant.plugin.java.impl.scanner.ClassFileScannerPlugin;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.*;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.HamcrestCondition.matching;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

/**
 * Verifies scanning of Java generic types.
 */
@Slf4j
class JavaGenericsIT extends AbstractJavaPluginIT {

    @Override
    protected Map<String, Object> getScannerProperties() {
        return Map.of(ClassFileScannerPlugin.PROPERTY_INCLUDE_LOCAL_VARIABLES, true);
    }

    @Test
    void outerClassTypeParameters() {
        scanClasses(GenericTypeDeclarations.class);
        store.beginTransaction();
        List<TypeVariableDescriptor> typeParameters = query(
                "MATCH (:Type:GenericDeclaration{name:'GenericTypeDeclarations'})-[declares:DECLARES_TYPE_PARAMETER]->(typeParameter:Java:ByteCode:Bound:TypeVariable) "
                        + //
                        "RETURN typeParameter ORDER BY declares.index").getColumn("typeParameter");
        assertThat(typeParameters).hasSize(2);
        TypeVariableDescriptor x = typeParameters.get(0);
        assertThat(x.getName()).isEqualTo("X");
        List<BoundDescriptor> xBounds = x.getUpperBounds();
        assertThat(xBounds).hasSize(1);
        assertThat(xBounds.get(0).getRawType()).is(matching(typeDescriptor(Object.class)));
        TypeVariableDescriptor y = typeParameters.get(1);
        assertThat(y.getName()).isEqualTo("Y");
        List<BoundDescriptor> yBounds = y.getUpperBounds();
        assertThat(yBounds).hasSize(2);
        List<TypeDescriptor> rawYBounds = yBounds.stream().map(bound -> bound.getRawType()).collect(toList());
        assertThat(rawYBounds).is(matching(hasItems(typeDescriptor(Serializable.class), typeDescriptor(List.class))));
        store.commitTransaction();
    }

    @Test
    void innerClassTypeParameters() {
        scanClasses(GenericTypeDeclarations.Inner.class);
        store.beginTransaction();
        List<TypeVariableDescriptor> declaredTypeParameters = query(
                "MATCH (:Type:GenericDeclaration{name:'GenericTypeDeclarations$Inner'})-[declares:DECLARES_TYPE_PARAMETER]->(typeParameter:Java:ByteCode:Bound:TypeVariable) "
                        + //
                        "RETURN typeParameter ORDER BY declares.index").getColumn("typeParameter");
        assertThat(declaredTypeParameters).hasSize(1);
        TypeVariableDescriptor x = declaredTypeParameters.get(0);
        assertThat(x.getName()).isEqualTo("X");
        List<BoundDescriptor> xBounds = x.getUpperBounds();
        assertThat(xBounds).hasSize(1);
        assertThat(xBounds.get(0).getRawType()).is(matching(typeDescriptor(Object.class)));
        List<TypeVariableDescriptor> requiredTypeParameters = query(
                "MATCH (:Type:GenericDeclaration{name:'GenericTypeDeclarations$Inner'})-[declares:REQUIRES_TYPE_PARAMETER]->(typeParameter:Java:ByteCode:Bound:TypeVariable) "
                        + //
                        "RETURN typeParameter").getColumn("typeParameter");
        assertThat(requiredTypeParameters).hasSize(1);
        TypeVariableDescriptor y = requiredTypeParameters.get(0);
        assertThat(y.getName()).isEqualTo("Y");
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
        ParameterizedTypeDescriptor parameterizedType = parameterizedTypes.get(0);
        assertThat(parameterizedType.getRawType()).is(matching(typeDescriptor(List.class)));
        Map<Integer, BoundDescriptor> actualTypeArguments = getActualTypeArguments(parameterizedType, 1);
        assertThat(actualTypeArguments.get(0).getRawType()).is(matching(typeDescriptor(String.class)));
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
        ParameterizedTypeDescriptor parameterizedType = parameterizedTypes.get(0);
        assertThat(parameterizedType.getRawType()).is(matching(typeDescriptor(AbstractList.class)));
        Map<Integer, BoundDescriptor> actualTypeArguments = getActualTypeArguments(parameterizedType, 1);
        assertThat(actualTypeArguments.get(0).getRawType()).is(matching(typeDescriptor(String.class)));
        store.commitTransaction();
    }

    @Test
    void fieldOfTypeVariable() {
        scanClasses(GenericFields.class);
        store.beginTransaction();
        FieldDescriptor field = getMember("GenericFields", "typeVariable");
        assertThat(field.getType()).is(matching(typeDescriptor(Object.class)));
        verifyTypeVariable(field.getGenericType(), "X", typeDescriptor(GenericFields.class), Object.class);
        store.commitTransaction();
    }

    @Test
    void fieldOfArrayOfTypeVariable() {
        scanClasses(GenericFields.class);
        store.beginTransaction();
        FieldDescriptor field = getMember("GenericFields", "arrayOfTypeVariable");
        assertThat(field.getType()).is(matching(typeDescriptor(Object.class)));
        BoundDescriptor bound = field.getGenericType();
        assertThat(bound).isInstanceOf(GenericArrayTypeDescriptor.class);
        verifyTypeVariable(((GenericArrayTypeDescriptor) bound).getComponentType(), "X", typeDescriptor(GenericFields.class), Object.class);
        store.commitTransaction();
    }

    @Test
    void fieldOfArrayOfPrimitive() {
        scanClasses(GenericFields.class);
        store.beginTransaction();
        FieldDescriptor field = getMember("GenericFields", "arrayOfPrimitive");
        assertThat(field.getType()).is(matching(typeDescriptor(List.class)));
        BoundDescriptor bound = field.getGenericType();
        assertThat(bound).isInstanceOf(ParameterizedTypeDescriptor.class);
        ParameterizedTypeDescriptor parameterizedType = (ParameterizedTypeDescriptor) bound;
        List<HasActualTypeArgumentDescriptor> actualTypeArguments = parameterizedType.getActualTypeArguments();
        assertThat(actualTypeArguments).hasSize(1);
        BoundDescriptor typeArgument = actualTypeArguments.get(0).getTypeArgument();
        assertThat(typeArgument).isInstanceOf(GenericArrayTypeDescriptor.class);
        BoundDescriptor componentType = ((GenericArrayTypeDescriptor) typeArgument).getComponentType();
        assertThat(componentType.getRawType()).is(matching(typeDescriptor(boolean.class)));
        store.commitTransaction();
    }

    @Test
    void fieldOfParameterizedType() {
        scanClasses(GenericFields.class);
        store.beginTransaction();
        FieldDescriptor field = getMember("GenericFields", "parameterizedType");
        assertThat(field.getType()).is(matching(typeDescriptor(Map.class)));
        BoundDescriptor genericType = field.getGenericType();
        assertThat(genericType).isNotNull().isInstanceOf(ParameterizedTypeDescriptor.class);
        ParameterizedTypeDescriptor parameterizedType = (ParameterizedTypeDescriptor) genericType;
        assertThat(parameterizedType.getRawType()).is(matching(typeDescriptor(Map.class)));
        Map<Integer, BoundDescriptor> typeArguments = getActualTypeArguments(parameterizedType, 2);
        assertThat(typeArguments.get(0).getRawType()).is(matching(typeDescriptor(String.class)));
        verifyTypeVariable(typeArguments.get(1), "X", typeDescriptor(GenericFields.class), Object.class);
        store.commitTransaction();
    }

    @Test
    void fieldOfNestedParameterizedType() {
        scanClasses(GenericFields.class);
        store.beginTransaction();
        FieldDescriptor field = getMember("GenericFields", "nestedParameterizedType");
        assertThat(field.getType()).is(matching(typeDescriptor(List.class)));
        BoundDescriptor genericType = field.getGenericType();
        assertThat(genericType).isNotNull().isInstanceOf(ParameterizedTypeDescriptor.class);
        ParameterizedTypeDescriptor outerListType = (ParameterizedTypeDescriptor) genericType;
        assertThat(outerListType.getRawType()).is(matching(typeDescriptor(List.class)));
        Map<Integer, BoundDescriptor> outerTypeArguments = getActualTypeArguments(outerListType, 1);
        BoundDescriptor outerBound = outerTypeArguments.get(0);
        assertThat(outerBound.getRawType()).is(matching(typeDescriptor(List.class)));
        assertThat(outerBound).isInstanceOf(ParameterizedTypeDescriptor.class);
        ParameterizedTypeDescriptor innerListType = (ParameterizedTypeDescriptor) outerBound;
        Map<Integer, BoundDescriptor> innerTypeArguments = getActualTypeArguments(innerListType, 1);
        BoundDescriptor boundDescriptor = innerTypeArguments.get(0);
        assertThat(boundDescriptor.getRawType()).is(matching(typeDescriptor(String.class)));
        store.commitTransaction();
    }

    @Test
    void fieldOfUpperBoundWildcard() {
        scanClasses(GenericFields.class);
        store.beginTransaction();
        FieldDescriptor field = getMember("GenericFields", "upperBoundWildcard");
        assertThat(field.getType()).is(matching(typeDescriptor(List.class)));
        BoundDescriptor genericType = field.getGenericType();
        WildcardTypeDescriptor wildcardType = getListOfWildcard(genericType);
        assertThat(wildcardType.getLowerBounds()).isEmpty();
        verifyWildcardBounds(wildcardType.getUpperBounds());
        store.commitTransaction();
    }

    @Test
    void fieldOfLowerBoundWildcard() {
        scanClasses(GenericFields.class);
        store.beginTransaction();
        FieldDescriptor field = getMember("GenericFields", "lowerBoundWildcard");
        assertThat(field.getType()).is(matching(typeDescriptor(List.class)));
        BoundDescriptor genericType = field.getGenericType();
        WildcardTypeDescriptor wildcardType = getListOfWildcard(genericType);
        verifyWildcardBounds(wildcardType.getLowerBounds());
        assertThat(wildcardType.getUpperBounds()).isEmpty();
        store.commitTransaction();
    }

    @Test
    void fieldOfUnboundWildcard() {
        scanClasses(GenericFields.class);
        store.beginTransaction();
        FieldDescriptor field = getMember("GenericFields", "unboundWildcard");
        assertThat(field.getType()).is(matching(typeDescriptor(List.class)));
        BoundDescriptor genericType = field.getGenericType();
        WildcardTypeDescriptor wildcardType = getListOfWildcard(genericType);
        assertThat(wildcardType.getLowerBounds()).isEmpty();
        assertThat(wildcardType.getUpperBounds()).isEmpty();
        store.commitTransaction();
    }

    @Test
    void genericParameter() {
        scanClasses(GenericMethods.class);
        store.beginTransaction();
        MethodDescriptor genericParameter = getMember("GenericMethods", "genericParameter");
        List<ParameterDescriptor> parameters = genericParameter.getParameters();
        assertThat(parameters).hasSize(1);
        ParameterDescriptor parameter = parameters.get(0);
        assertThat(parameter.getIndex()).isZero();
        assertThat(parameter.getType()).is(matching(typeDescriptor(Object.class)));
        verifyTypeVariable(parameter.getGenericType(), "X", typeDescriptor(GenericMethods.class), Object.class);
        store.commitTransaction();
    }

    @Test
    void genericReturnType() {
        scanClasses(GenericMethods.class);
        store.beginTransaction();
        MethodDescriptor genericReturnType = getMember("GenericMethods", "genericReturnType");
        assertThat(genericReturnType.getReturns()).is(matching(typeDescriptor(Object.class)));
        verifyTypeVariable(genericReturnType.getReturnsGeneric(), "X", typeDescriptor(GenericMethods.class), Object.class);
        store.commitTransaction();
    }

    @Test
    void genericException() throws NoSuchMethodException {
        scanClasses(GenericMethods.class);
        store.beginTransaction();
        MethodDescriptor genericException = getMember("GenericMethods", "genericException");
        List<TypeDescriptor> throwsExceptions = genericException.getThrows();
        assertThat(throwsExceptions).hasSize(1);
        assertThat(throwsExceptions.get(0)).is(matching(typeDescriptor(IOException.class)));
        List<BoundDescriptor> throwsGenericExceptions = genericException.getThrowsGeneric();
        assertThat(throwsGenericExceptions).hasSize(1);
        verifyTypeVariable(throwsGenericExceptions.get(0), "E", methodDescriptor(GenericMethods.class, "genericException"), IOException.class);
        store.commitTransaction();
    }

    @Test
    void overwriteTypeParameter() throws NoSuchMethodException {
        scanClasses(GenericMethods.class);
        store.beginTransaction();
        MethodDescriptor overwriteTypeParameter = getMember("GenericMethods", "overwriteTypeParameter");
        assertThat(overwriteTypeParameter.getReturns()).is(matching(typeDescriptor(Object.class)));
        verifyTypeVariable(overwriteTypeParameter.getReturnsGeneric(), "X", methodDescriptor(GenericMethods.class, "overwriteTypeParameter"), Object.class);
        store.commitTransaction();
    }

    @Test
    void genericVariable() {
        scanClasses(GenericMethods.class);
        store.beginTransaction();
        MethodDescriptor genericVariable = getMember("GenericMethods", "genericVariable");
        List<VariableDescriptor> variables = genericVariable.getVariables();
        assertThat(variables).hasSize(1);
        VariableDescriptor x = variables.get(0);
        assertThat(x.getType()).is(matching(typeDescriptor(Object.class)));
        verifyTypeVariable(x.getGenericType(), "X", typeDescriptor(GenericMethods.class), Object.class);
        store.commitTransaction();
    }

    @Test
    void variableOfParameterizedType() {
        scanClasses(GenericFields.class);
        store.beginTransaction();
        Map<String, Object> parameters = MapBuilder.<String, Object> builder().entry("typeName", "GenericFields").entry("variable", "parameterizedType").build();
        List<VariableDescriptor> variables = query("MATCH (:Type{name:$typeName})-[:DECLARES]->(:Java:ByteCode:Method)-[:DECLARES]->(variable:Variable{name:$variable}) RETURN variable", parameters)
            .getColumn("variable");
        assertThat(variables).hasSize(1);
        VariableDescriptor variable = variables.get(0);
        assertThat(variable.getType()).is(matching(typeDescriptor(Map.class)));
        BoundDescriptor genericType = variable.getGenericType();
        assertThat(genericType).isNotNull().isInstanceOf(ParameterizedTypeDescriptor.class);
        ParameterizedTypeDescriptor parameterizedType = (ParameterizedTypeDescriptor) genericType;
        assertThat(((BoundDescriptor)parameterizedType).getRawType()).is(matching(typeDescriptor(Map.class)));
        Map<Integer, BoundDescriptor> typeArguments = getActualTypeArguments(parameterizedType, 2);
        assertThat(typeArguments.get(0).getRawType()).is(matching(typeDescriptor(String.class)));
        verifyTypeVariable(typeArguments.get(1), "X", typeDescriptor(GenericFields.class), Object.class);
        store.commitTransaction();
    }

    private <T extends MemberDescriptor> T getMember(String typeName, String memberName) {
        Map<String, Object> parameters = MapBuilder.<String, Object> builder().entry("typeName", typeName).entry("memberName", memberName).build();
        List<T> members = query("MATCH (:Type{name:$typeName})-[:DECLARES]->(member:Java:ByteCode:Member{name:$memberName}) RETURN member", parameters)
                .getColumn("member");
        assertThat(members).hasSize(1);
        return members.get(0);
    }

    private WildcardTypeDescriptor getListOfWildcard(BoundDescriptor genericType) {
        assertThat(genericType).isNotNull().isInstanceOf(ParameterizedTypeDescriptor.class);
        ParameterizedTypeDescriptor parameterizedType = (ParameterizedTypeDescriptor) genericType;
        assertThat(((BoundDescriptor)parameterizedType).getRawType()).is(matching(typeDescriptor(List.class)));
        Map<Integer, BoundDescriptor> typeArguments = getActualTypeArguments(parameterizedType, 1);
        BoundDescriptor boundDescriptor = typeArguments.get(0);
        assertThat(boundDescriptor).isInstanceOf(WildcardTypeDescriptor.class);
        return (WildcardTypeDescriptor) boundDescriptor;
    }

    private void verifyWildcardBounds(List<BoundDescriptor> bounds) {
        assertThat(bounds).hasSize(1);
        BoundDescriptor upperBound = bounds.get(0);
        assertThat(upperBound).isInstanceOf(TypeVariableDescriptor.class);
        TypeVariableDescriptor typeVariable = (TypeVariableDescriptor) upperBound;
        assertThat(typeVariable.getName()).isEqualTo("X");
    }

    private void verifyTypeVariable(BoundDescriptor bound, String expectedName, Matcher<?> declaredBy, Class<?> expectedRawType) {
        assertThat(bound).isNotNull().isInstanceOf(TypeVariableDescriptor.class);
        TypeVariableDescriptor typeVariable = (TypeVariableDescriptor) bound;
        assertThat(typeVariable.getName()).isEqualTo(expectedName);
        assertThat(typeVariable.getDeclaredBy()).is(matching(declaredBy));
        assertThat(typeVariable.getRawType()).is(matching(typeDescriptor(expectedRawType)));
    }

    private Map<Integer, BoundDescriptor> getActualTypeArguments(ParameterizedTypeDescriptor parameterizedType, int expectedTypeArgumentCount) {
        List<HasActualTypeArgumentDescriptor> actualTypeArguments = parameterizedType.getActualTypeArguments();
        assertThat(actualTypeArguments).hasSize(expectedTypeArgumentCount);
        Map<Integer, BoundDescriptor> typeArguments = actualTypeArguments.stream().collect(toMap(a -> a.getIndex(), a -> a.getTypeArgument()));
        return typeArguments;
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
