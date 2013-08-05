package com.buschmais.jqassistant.scanner.impl.visitor;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.ClassDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.FieldDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.MethodDescriptor;
import com.buschmais.jqassistant.scanner.impl.resolver.DescriptorResolverFactory;

public class ClassVisitor extends AbstractVisitor implements org.objectweb.asm.ClassVisitor {

    private ClassDescriptor classDescriptor;
	private String artifactIdentifier;

	public ClassVisitor(DescriptorResolverFactory resolverFactory, String artifactIdentifier) {
        super(resolverFactory);
		this.artifactIdentifier = artifactIdentifier;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        classDescriptor = getClassDescriptor(name);

		if (artifactIdentifier != null) {
			ArtifactDescriptor descriptor = getStore().findArtifactDescriptor(artifactIdentifier);
			if (descriptor == null) {
				descriptor = getStore().createArtifactDescriptor(artifactIdentifier);
			}
			descriptor.getContains().add(classDescriptor);
		}

        if (signature == null) {
            if (superName != null) {
                classDescriptor.setSuperClass(getClassDescriptor(superName));
            }
            for (int i = 0; interfaces != null && i < interfaces.length; i++) {
                classDescriptor.getInterfaces().add(getClassDescriptor(interfaces[i]));
            }
        } else {
            new SignatureReader(signature).accept(new ClassSignatureVisitor(classDescriptor, getResolverFactory()));
        }
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        FieldDescriptor fieldDescriptor = getFielDescriptor(classDescriptor, name, desc);
        classDescriptor.getContains().add(fieldDescriptor);
        if (signature == null) {
            addDependency(fieldDescriptor, getType((desc)));
        } else {
            new SignatureReader(signature).accept(new DependentTypeSignatureVisitor(fieldDescriptor, getResolverFactory()));
        }
        if (value instanceof Type) {
            addDependency(fieldDescriptor, getType((Type) value));
        }
        return new FieldVisitor(fieldDescriptor, getResolverFactory());
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodDescriptor methodDescriptor = getMethodDescriptor(classDescriptor, name, desc);
        classDescriptor.getContains().add(methodDescriptor);
        if (signature == null) {
            addDependency(methodDescriptor, getType(Type.getReturnType(desc)));
            Type[] types = Type.getArgumentTypes(desc);
            for (int i = 0; i < types.length; i++) {
                addDependency(methodDescriptor, getType(types[i]));
            }
        } else {
            new SignatureReader(signature).accept(new MethodSignatureVisitor(methodDescriptor, getResolverFactory()));
        }
        for (int i = 0; exceptions != null && i < exceptions.length; i++) {
            ClassDescriptor exception = getClassDescriptor(Type.getObjectType(exceptions[i]).getClassName());
            methodDescriptor.getDeclaredThrowables().add(exception);
        }
        return new MethodVisitor(methodDescriptor, getResolverFactory());
    }

    @Override
    public void visitSource(final String source, final String debug) {
    }

    @Override
    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
        // addName( outerName);
        // addName( innerName);
    }

    @Override
    public void visitOuterClass(final String owner, final String name, final String desc) {
        // addName(owner);
        // addMethodDesc(desc);
    }

    // ---------------------------------------------

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        addAnnotation(classDescriptor, getType(desc));
        return new AnnotationVisitor(classDescriptor, getResolverFactory());
    }

    @Override
    public void visitAttribute(Attribute attribute) {
    }

    @Override
    public void visitEnd() {
    }

    protected MethodDescriptor getMethodDescriptor(ClassDescriptor classDescriptor, String name, String desc) {
        MethodDescriptor methodDescriptor = getStore().createMethodDescriptor(classDescriptor, getMethodSignature(name, desc));
        return methodDescriptor;
    }

    protected FieldDescriptor getFielDescriptor(ClassDescriptor classDescriptor, String name, String desc) {
        FieldDescriptor fieldDescriptor = getStore().createFieldDescriptor(classDescriptor, getFieldSignature(name, desc));
        return fieldDescriptor;
    }

    private String getMethodSignature(String name, String desc) {
        StringBuffer signature = new StringBuffer();
        String returnType = Type.getReturnType(desc).getClassName();
        if (returnType != null) {
            signature.append(returnType);
            signature.append(' ');
        }
        signature.append(name);
        signature.append('(');
        Type[] types = Type.getArgumentTypes(desc);
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                signature.append(',');
            }
            signature.append(types[i].getClassName());
        }
        signature.append(')');
        return signature.toString();
    }

    private String getFieldSignature(String name, String desc) {
        StringBuffer signature = new StringBuffer();
        String returnType = Type.getReturnType(desc).getClassName();
        signature.append(returnType);
        signature.append(' ');
        signature.append(name);
        return signature.toString();
    }

}
