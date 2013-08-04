package com.buschmais.jqassistant.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.MethodDescriptor;
import com.buschmais.jqassistant.scanner.impl.resolver.DescriptorResolverFactory;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Created with IntelliJ IDEA.
 * User: Dirk Mahler
 * Date: 03.08.13
 * Time: 09:17
 * To change this template use File | Settings | File Templates.
 */
public class MethodSignatureVisitor extends AbstractVisitor implements SignatureVisitor {

    private MethodDescriptor methodDescriptor;

    MethodSignatureVisitor(MethodDescriptor methodDescriptor, DescriptorResolverFactory resolverFactory) {
        super(resolverFactory);
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public void visitFormalTypeParameter(String name) {

    }

    @Override
    public SignatureVisitor visitClassBound() {
        return new DependentTypeSignatureVisitor(methodDescriptor,getResolverFactory());
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return new DependentTypeSignatureVisitor(methodDescriptor,getResolverFactory());
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitInterface() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitParameterType() {
        return new DependentTypeSignatureVisitor(methodDescriptor,getResolverFactory());
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return new DependentTypeSignatureVisitor(methodDescriptor,getResolverFactory());
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        return new DependentTypeSignatureVisitor(methodDescriptor,getResolverFactory());
    }

    @Override
    public void visitBaseType(char descriptor) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public void visitTypeVariable(String name) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitArrayType() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public void visitClassType(String name) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public void visitInnerClassType(String name) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public void visitTypeArgument() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public void visitEnd() {
    }
}
