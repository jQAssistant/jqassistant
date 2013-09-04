package com.buschmais.jqassistant.core.model.api.descriptor;

/**
 * Describes a Java package.
 */
public class PackageDescriptor extends ParentDescriptor implements SignatureDescriptor {

    /**
     * The signature of the package.
     */
    private String signature;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
