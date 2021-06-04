package com.buschmais.jqassistant.plugin.java.test.scanner.generics;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.AbstractList;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.HasActualTypeArgumentDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.ParameterizedTypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.TypeVariableDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.ExtendsGeneric;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericFields;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.GenericTypeDeclarations;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.ImplementsGeneric;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

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
        TypeVariableDescriptor x = typeParameters.get(0);
        assertThat(x.getName().equals("X"));
        List<BoundDescriptor> xBounds = x.getBounds();
        assertThat(xBounds.size()).isEqualTo(1);
        assertThat(xBounds.get(0).getRawType()).is(matching(typeDescriptor(Object.class)));
        TypeVariableDescriptor y = typeParameters.get(1);
        assertThat(y.getName().equals("Y"));
        List<BoundDescriptor> yBounds = y.getBounds();
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
        assertThat(x.getName().equals("X"));
        List<BoundDescriptor> xBounds = x.getBounds();
        assertThat(xBounds.size()).isEqualTo(1);
        assertThat(xBounds.get(0).getRawType()).is(matching(typeDescriptor(Object.class)));
        List<TypeVariableDescriptor> requiredTypeParameters = query(
                "MATCH (:Type:GenericDeclaration{name:'GenericTypeDeclarations$Inner'})-[declares:REQUIRES_TYPE_PARAMETER]->(typeParameter:Java:ByteCode:Bound:TypeVariable) "
                        + //
                        "RETURN typeParameter").getColumn("typeParameter");
        assertThat(requiredTypeParameters).hasSize(1);
        TypeVariableDescriptor y = declaredTypeParameters.get(0);
        assertThat(y.getName().equals("Y"));
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

    @Test
    void fieldOfTypeVariable() {
        scanClasses(GenericFields.class);
        store.beginTransaction();
        List<FieldDescriptor> fields = query("MATCH (:Type{name:'GenericFields'})-[:DECLARES]->(field:Java:ByteCode:Field{name:'typeVariable'}) RETURN field")
                .getColumn("field");
        assertThat(fields).hasSize(1);
        FieldDescriptor field = fields.get(0);
        BoundDescriptor genericType = field.getGenericType();
        verifyTypeVariable(genericType, "X", typeDescriptor(GenericFields.class));
        store.commitTransaction();
    }

    @Test
    void fieldOfParameterizedType() {
        scanClasses(GenericFields.class);
        store.beginTransaction();
        List<FieldDescriptor> fields = query(
                "MATCH (:Type{name:'GenericFields'})-[:DECLARES]->(field:Java:ByteCode:Field{name:'parameterizedType'}) RETURN field").getColumn("field");
        assertThat(fields).hasSize(1);
        FieldDescriptor field = fields.get(0);
        BoundDescriptor genericType = field.getGenericType();
        assertThat(genericType).isNotNull().isInstanceOf(ParameterizedTypeDescriptor.class);
        ParameterizedTypeDescriptor parameterizedType = (ParameterizedTypeDescriptor) genericType;
        assertThat(parameterizedType.getRawType()).is(matching(typeDescriptor(Map.class)));
        List<HasActualTypeArgumentDescriptor> actualTypeArguments = parameterizedType.getActualTypeArguments();
        assertThat(actualTypeArguments).hasSize(2);
        Map<Integer, BoundDescriptor> typeArguments = actualTypeArguments.stream().collect(toMap(a -> a.getIndex(), a -> a.getTypeArgument()));
        assertThat(typeArguments.get(0).getRawType()).is(matching(typeDescriptor(String.class)));
        verifyTypeVariable(typeArguments.get(1), "X", typeDescriptor(GenericFields.class));
        store.commitTransaction();
    }

    private void verifyTypeVariable(BoundDescriptor bound, String expectedName, Matcher<?> declaredBy) {
        assertThat(bound).isNotNull().isInstanceOf(TypeVariableDescriptor.class);
        TypeVariableDescriptor typeVariable = (TypeVariableDescriptor) bound;
        assertThat(typeVariable.getName()).isEqualTo(expectedName);
        assertThat(typeVariable.getDeclaredBy().getGenericDeclaration()).is(matching(declaredBy));
    }

    private void verifyParameterizedType(ParameterizedTypeDescriptor parameterizedType, Class<?> rawType, Class<?> typeArgument) {
        assertThat(parameterizedType.getRawType()).is(matching(typeDescriptor(rawType)));
        List<HasActualTypeArgumentDescriptor> actualTypeArguments = parameterizedType.getActualTypeArguments();
        assertThat(actualTypeArguments).hasSize(1);
        HasActualTypeArgumentDescriptor hasActualTypeArgument = actualTypeArguments.get(0);
        assertThat(hasActualTypeArgument.getIndex()).isEqualTo(0);
        BoundDescriptor stringType = hasActualTypeArgument.getTypeArgument();
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
