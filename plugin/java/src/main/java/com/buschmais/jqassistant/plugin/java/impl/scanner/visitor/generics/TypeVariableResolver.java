package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import java.util.*;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.GenericDeclarationDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.TypeVariableDescriptor;

import lombok.RequiredArgsConstructor;

public class TypeVariableResolver {

    private final Deque<TypeVariableDeclarations> typeVariableDeclarations = new LinkedList<>();

    public void push(GenericDeclarationDescriptor genericDeclaration) {
        typeVariableDeclarations.offerFirst(new TypeVariableDeclarations(genericDeclaration));
    }

    public void pop() {
        typeVariableDeclarations.removeFirst();
    }

    public void declare(TypeVariableDescriptor typeVariable) {
        typeVariableDeclarations.peek().put(typeVariable);
    }

    public TypeVariableDescriptor resolve(String name, TypeDescriptor containingType) {
        Optional<TypeVariableDescriptor> typeVariableOptional = typeVariableDeclarations.stream().map(genericDeclarations -> genericDeclarations.get(name))
                .filter(value -> value != null).findFirst();
        if (typeVariableOptional.isPresent()) {
            return typeVariableOptional.get();
        }
        // No declaration found, add a required type variable to the containing type
        return containingType.requireTypeParameter(name);
    }

    @RequiredArgsConstructor
    private static class TypeVariableDeclarations {

        private final GenericDeclarationDescriptor genericDeclarations;

        private final Map<String, TypeVariableDescriptor> typeVariables = new HashMap<>();

        void put(TypeVariableDescriptor typeVariable) {
            typeVariables.put(typeVariable.getName(), typeVariable);
        }

        TypeVariableDescriptor get(String name) {
            return typeVariables.get(name);
        }

    }

}
