package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;

public class ClassVisitor extends AbstractVisitor implements
		org.objectweb.asm.ClassVisitor {

	private ClassDescriptor classDescriptor;

	protected ClassVisitor(Store store) {
		super(store);
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		classDescriptor = getClassDescriptor(name);
		if (signature == null) {
			if (superName != null) {
				classDescriptor.addSuperClass(getClassDescriptor(superName));
			}
			for (int i = 0; interfaces != null && i < interfaces.length; i++) {
				classDescriptor
						.addImplements(getClassDescriptor(getInternalName(interfaces[i])));
			}
		} else {
			new SignatureReader(signature).accept(new ClassSignatureVisitor(
					getStore(), classDescriptor));
		}
	}

	@Override
	public FieldVisitor visitField(final int access, final String name,
			final String desc, final String signature, final Object value) {
		if (signature == null) {
			addDependency(classDescriptor, getType((desc)));
		} else {
			addDependency(classDescriptor,
					getTypeSignature(signature, classDescriptor));
		}
		if (value instanceof Type) {
			addDependency(classDescriptor, getType((Type) value));
		}
		return new FieldVisitor(getStore(), classDescriptor);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {
		getMethodDescriptor(classDescriptor, name, desc);
		if (signature == null) {
			addMethodDesc(desc);
		} else {
			new SignatureReader(signature).accept(new MethodSignatureVisitor(
					getStore(), classDescriptor));
		}
		addInternalNames(exceptions);
		return new MethodVisitor(getStore(), classDescriptor);
	}

	@Override
	public void visitSource(final String source, final String debug) {
	}

	@Override
	public void visitInnerClass(final String name, final String outerName,
			final String innerName, final int access) {
		// addName( outerName);
		// addName( innerName);
	}

	@Override
	public void visitOuterClass(final String owner, final String name,
			final String desc) {
		// addName(owner);
		// addMethodDesc(desc);
	}

	// ---------------------------------------------

	private void addInternalNames(final String[] names) {
		for (int i = 0; names != null && i < names.length; i++) {
			addDependency(classDescriptor, getInternalName(names[i]));
		}
	}

	private void addMethodDesc(final String desc) {
		addDependency(classDescriptor, getType(Type.getReturnType(desc)));
		Type[] types = Type.getArgumentTypes(desc);
		for (int i = 0; i < types.length; i++) {
			addDependency(classDescriptor, getType(types[i]));
		}
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc,
			final boolean visible) {
		addDependency(classDescriptor, getType(desc));
		return new AnnotationVisitor(getStore(), classDescriptor);
	}

	@Override
	public void visitAttribute(Attribute attribute) {
	}

	@Override
	public void visitEnd() {
	}

}
