package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

import static com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper.ASM_OPCODES;

/**
 * Overrides functionality of {@link SimpleVerifier} that relies on loading classes and implements best guesses about class types and hierarchies.
 */
final class MethodDataFlowVerifier extends SimpleVerifier {

    private final Type type;
    private final Type superType;
    private final List<Type> interfaceTypes;
    private final boolean isInterfaceType;

    protected MethodDataFlowVerifier(Type type, final boolean isInterface, Type superType, List<Type> interfaceTypes) {
        super(ASM_OPCODES, type, superType, interfaceTypes, isInterface);
        this.type = type;
        this.superType = superType;
        this.interfaceTypes = interfaceTypes;
        this.isInterfaceType = isInterface;
    }

    @Override
    protected boolean isAssignableFrom(Type type1, Type type2) {
        return true;
    }

    @Override
    protected boolean isInterface(Type type) {
        return this.type.equals(type) ?
                this.isInterfaceType :
                this.interfaceTypes.stream()
                        .anyMatch(interfaceType -> interfaceType.equals(type));
    }

    @Override
    protected Type getSuperClass(Type type) {
        if (this.type.equals(type)) {
            return this.superType;
        }
        return null;
    }
}
