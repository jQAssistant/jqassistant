package com.buschmais.jqassistant.plugin.java.api;

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
     * Return a method signature.
     *
     * @param name         The method name.
     * @param rawSignature The signature containing parameter, return and exception values.
     * @return The method signature.
     */
    public static String getMethodSignature(String name, String rawSignature) {
        StringBuffer signature = new StringBuffer();
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
     * @param name         The field name.
     * @param rawSignature The signature containing the type value.
     * @return The field signature.
     */
    public static String getFieldSignature(String name, String rawSignature) {
        StringBuffer signature = new StringBuffer();
        String returnType = org.objectweb.asm.Type.getReturnType(rawSignature).getClassName();
        signature.append(returnType);
        signature.append(' ');
        signature.append(name);
        return signature.toString();
    }
}
