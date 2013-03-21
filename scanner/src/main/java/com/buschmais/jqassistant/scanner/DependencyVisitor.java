package com.buschmais.jqassistant.scanner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;

import com.buschmais.jqassistant.store.model.ClassDescriptor;
import com.buschmais.jqassistant.store.model.PackageDescriptor;

public class DependencyVisitor extends AbstractVisitor implements
		AnnotationVisitor, ClassVisitor, FieldVisitor, MethodVisitor {

	ClassDescriptor current;

	private final Map<String, ClassDescriptor> classDescriptors = new HashMap<String, ClassDescriptor>();

	private final Map<String, PackageDescriptor> packageDescriptors = new HashMap<String, PackageDescriptor>();

	private final Map<ClassDescriptor, Set<ClassDescriptor>> dependencies = new HashMap<ClassDescriptor, Set<ClassDescriptor>>();

	// ClassVisitor
	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces) {
		current = getClassDescriptor(name);
		if (signature == null) {
			addDependency(getInternalName(superName));
			addInternalNames(interfaces);
		} else {
			addSignature(signature);
		}
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc,
			final boolean visible) {
		addDesc(desc);
		return this;
	}

	@Override
	public void visitAttribute(final Attribute attr) {
	}

	@Override
	public FieldVisitor visitField(final int access, final String name,
			final String desc, final String signature, final Object value) {
		if (signature == null) {
			addDesc(desc);
		} else {
			addTypeSignature(signature);
		}
		if (value instanceof Type) {
			addDependency(getType((Type) value));
		}
		return this;
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature, final String[] exceptions) {
		if (signature == null) {
			addMethodDesc(desc);
		} else {
			addSignature(signature);
		}
		addInternalNames(exceptions);
		return this;
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

	// MethodVisitor

	@Override
	public AnnotationVisitor visitParameterAnnotation(final int parameter,
			final String desc, final boolean visible) {
		addDesc(desc);
		return this;
	}

	@Override
	public void visitTypeInsn(final int opcode, final String type) {
		addDependency(getType(Type.getObjectType(type)));
	}

	@Override
	public void visitFieldInsn(final int opcode, final String owner,
			final String name, final String desc) {
		addDependency(getInternalName(owner));
		addDesc(desc);
	}

	@Override
	public void visitMethodInsn(final int opcode, final String owner,
			final String name, final String desc) {
		addDependency(getInternalName(owner));
		addMethodDesc(desc);
	}

	@Override
	public void visitLdcInsn(final Object cst) {
		if (cst instanceof Type) {
			addDependency(getType((Type) cst));
		}
	}

	@Override
	public void visitMultiANewArrayInsn(final String desc, final int dims) {
		addDesc(desc);
	}

	@Override
	public void visitLocalVariable(final String name, final String desc,
			final String signature, final Label start, final Label end,
			final int index) {
		addTypeSignature(signature);
	}

	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		return this;
	}

	@Override
	public void visitCode() {
	}

	@Override
	public void visitFrame(final int type, final int nLocal,
			final Object[] local, final int nStack, final Object[] stack) {
	}

	@Override
	public void visitInsn(final int opcode) {
	}

	@Override
	public void visitIntInsn(final int opcode, final int operand) {
	}

	@Override
	public void visitVarInsn(final int opcode, final int var) {
	}

	@Override
	public void visitJumpInsn(final int opcode, final Label label) {
	}

	@Override
	public void visitLabel(final Label label) {
	}

	@Override
	public void visitIincInsn(final int var, final int increment) {
	}

	@Override
	public void visitTableSwitchInsn(final int min, final int max,
			final Label dflt, final Label[] labels) {
	}

	@Override
	public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
			final Label[] labels) {
	}

	@Override
	public void visitTryCatchBlock(final Label start, final Label end,
			final Label handler, final String type) {
		addDependency(getInternalName(type));
	}

	@Override
	public void visitLineNumber(final int line, final Label start) {
	}

	@Override
	public void visitMaxs(final int maxStack, final int maxLocals) {
	}

	// AnnotationVisitor

	@Override
	public void visit(final String name, final Object value) {
		if (value instanceof Type) {
			addDependency(getType((Type) value));
		}
	}

	@Override
	public void visitEnum(final String name, final String desc,
			final String value) {
		addDesc(desc);
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String name,
			final String desc) {
		addDesc(desc);
		return this;
	}

	@Override
	public AnnotationVisitor visitArray(final String name) {
		return this;
	}

	// ---------------------------------------------

	private ClassDescriptor getClassDescriptor(String name) {
		ClassDescriptor classDescriptor = classDescriptors.get(name);
		if (classDescriptor == null) {
			String fullQualifiedName = name.replace("/", ".");
			int n = fullQualifiedName.lastIndexOf('.');
			String packageName;
			String className;
			if (n > -1) {
				packageName = fullQualifiedName.substring(0, n);
				className = fullQualifiedName.substring(n + 1,
						fullQualifiedName.length());
			} else {
				className = fullQualifiedName;
				packageName = "";
			}
			PackageDescriptor packageDescriptor = getPackageDescriptor(packageName);
			classDescriptor = new ClassDescriptor(packageDescriptor, className);
			classDescriptors.put(className, classDescriptor);
		}
		return classDescriptor;
	}

	private PackageDescriptor getPackageDescriptor(String packageName) {
		PackageDescriptor packageDescriptor = packageDescriptors
				.get(packageName);
		if (packageDescriptor == null) {
			int n = packageName.lastIndexOf('.');
			PackageDescriptor parent = null;
			String localName;
			if (n > -1) {
				parent = getPackageDescriptor(packageName.substring(0, n));
				localName = packageName.substring(n + 1, packageName.length());
			} else {
				localName = packageName;
			}
			packageDescriptor = new PackageDescriptor(parent, localName);
			this.packageDescriptors.put(packageName, packageDescriptor);
		}
		return packageDescriptor;
	}

	private void addInternalNames(final String[] names) {
		for (int i = 0; names != null && i < names.length; i++) {
			addDependency(getInternalName(names[i]));
		}
	}

	private void addDependency(final String name) {
		if (name == null) {
			return;
		}
		ClassDescriptor p = getClassDescriptor(name);
		Set<ClassDescriptor> dependsOn = this.dependencies.get(current);
		if (dependsOn == null) {
			dependsOn = new HashSet<ClassDescriptor>();
			this.dependencies.put(current, dependsOn);
		}
		dependsOn.add(p);
	}

	private void addDesc(final String desc) {
		addDependency(getType(Type.getType(desc)));
	}

	private void addMethodDesc(final String desc) {
		addDependency(getType(Type.getReturnType(desc)));
		Type[] types = Type.getArgumentTypes(desc);
		for (int i = 0; i < types.length; i++) {
			addDependency(getType(types[i]));
		}
	}

	private void addSignature(final String signature) {
		if (signature != null) {
			SignatureVisitor signatureVisitor = new SignatureVisitor();
			new SignatureReader(signature).accept(signatureVisitor);
			addDependency(signatureVisitor.getSignatureClassName());
		}
	}

	private void addTypeSignature(final String signature) {
		if (signature != null) {
			SignatureVisitor signatureVisitor = new SignatureVisitor();
			new SignatureReader(signature).acceptType(signatureVisitor);
			addDependency(signatureVisitor.getSignatureClassName());
		}
	}

	public Map<ClassDescriptor, Set<ClassDescriptor>> getDependencies() {
		return dependencies;
	}

}
