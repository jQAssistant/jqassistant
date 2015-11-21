package com.buschmais.jqassistant.plugin.java.api.scanner;

import org.objectweb.asm.Type;

/**
 * Provides utility functions for working with signatures.
 */
public final class SignatureHelper {

    /**
     * Private constructor.
     */
    private SignatureHelper() {
    }

    /**
     * Returns the Java type name corresponding to the given internal name.
     * 
     * @param desc
     *            The internal name.
     * @return The type name.
     */
    public static String getObjectType(String desc) {
        return getType(Type.getObjectType(desc));
    }

    /**
     * Returns the Java type name type corresponding to the given type
     * descriptor.
     * 
     * @param desc
     *            The type descriptor.
     * @return The type name.
     */
    public static String getType(String desc) {
        return getType(Type.getType(desc));
    }

    /**
     * Return the type name of the given ASM type.
     * 
     * @param t
     *            The ASM type.
     * @return The type name.
     */
    public static String getType(final Type t) {
        switch (t.getSort()) {
        case Type.ARRAY:
            return getType(t.getElementType());
        default:
            return t.getClassName();
        }
    }

    /**
     * Return a method signature.
     * 
     * @param name
     *            The method name.
     * @param rawSignature
     *            The signature containing parameter, return and exception
     *            values.
     * @return The method signature.
     */
    public static String getMethodSignature(String name, String rawSignature) {
        StringBuilder signature = new StringBuilder();
        String returnType = org.objectweb.asm.Type.getReturnType(rawSignature).getClassName();
        if (returnType != null) {
            signature.append(returnType);
            signature.append(' ');
        }
        signature.append(name);
        signature.append('(');
        org.objectweb.asm.Type[] types = org.objectweb.asm.Type.getArgumentTypes(rawSignature);
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                signature.append(',');
            }
            signature.append(types[i].getClassName());
        }
        signature.append(')');
        return signature.toString();
    }

    /**
     * Return a field signature.
     * 
     * @param name
     *            The field name.
     * @param rawSignature
     *            The signature containing the type value.
     * @return The field signature.
     */
    public static String getFieldSignature(String name, String rawSignature) {
        StringBuilder signature = new StringBuilder();
        String returnType = org.objectweb.asm.Type.getReturnType(rawSignature).getClassName();
        signature.append(returnType);
        signature.append(' ');
        signature.append(name);
        return signature.toString();
    }
}
