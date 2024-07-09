package com.buschmais.jqassistant.plugin.java.api.report;

import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.*;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Provides helper functionality related to Java Generics.
 */
public class JavaGenericsReportHelper {

    public String getSignature(TypeDescriptor typeDescriptor) {
        return typeDescriptor.getFullQualifiedName();
    }

    public String getSignature(FieldDescriptor fieldDescriptor) {
        BoundDescriptor genericType = fieldDescriptor.getGenericType();
        if (genericType != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(getSignature(genericType));
            builder.append(' ');
            builder.append(fieldDescriptor.getName());
            return builder.toString();
        }
        return fieldDescriptor.getSignature();
    }

    public String getSignature(BoundDescriptor bound) {
        if (bound != null) {
            if (bound instanceof TypeVariableDescriptor) {
                return getSignature(((TypeVariableDescriptor) bound));
            } else if (bound instanceof GenericArrayTypeDescriptor) {
                return getSignature(((GenericArrayTypeDescriptor) bound));
            } else if (bound instanceof ParameterizedTypeDescriptor) {
                return getSignature(((ParameterizedTypeDescriptor) bound));
            } else if (bound instanceof WildcardTypeDescriptor) {
                return getSignature(((WildcardTypeDescriptor) bound));
            } else {
                String rawTypeName = bound.getRawType().getFullQualifiedName();
                return rawTypeName.equals(Object.class.getName()) ? "" : rawTypeName;
            }
        }
        return "";
    }

    private String getSignature(TypeVariableDescriptor typeVariableDescriptor) {
        StringBuilder builder = new StringBuilder(typeVariableDescriptor.getName());
        builder.append(getBounds(typeVariableDescriptor.getUpperBounds(), "extends"));
        return builder.toString();
    }

    private String getSignature(ParameterizedTypeDescriptor parameterizedTypeDescriptor) {
        StringBuilder builder = new StringBuilder(parameterizedTypeDescriptor.getRawType().getFullQualifiedName());
        builder.append('<');
        List<HasActualTypeArgumentDescriptor> hasActualTypeArguments = parameterizedTypeDescriptor.getActualTypeArguments().stream().collect(toList());
        hasActualTypeArguments.sort(comparingInt(HasActualTypeArgumentDescriptor::getIndex));
        builder.append(
                hasActualTypeArguments.stream().map(hasActualTypeArgument -> getSignature(hasActualTypeArgument.getTypeArgument())).collect(joining(",")));
        builder.append('>');
        return builder.toString();
    }

    private String getSignature(WildcardTypeDescriptor wildcardTypeDescriptor) {
        StringBuilder builder = new StringBuilder("?");
        builder.append(getBounds(wildcardTypeDescriptor.getUpperBounds(), "extends"));
        builder.append(getBounds(wildcardTypeDescriptor.getLowerBounds(), "super"));
        return builder.toString();
    }

    private String getSignature(GenericArrayTypeDescriptor genericArrayTypeDescriptor) {
        StringBuilder builder = new StringBuilder(getSignature(genericArrayTypeDescriptor.getComponentType()));
        builder.append("[]");
        return builder.toString();
    }

    private String getBounds(List<BoundDescriptor> bounds, String type) {
        StringBuilder builder = new StringBuilder();
        String boundSignatures = bounds.stream().map(bound -> getSignature(bound)).collect(joining(" & "));
        if (!boundSignatures.isEmpty()) {
            builder.append(' ').append(type).append(' ').append(boundSignatures);
        }
        return builder.toString();
    }

}
