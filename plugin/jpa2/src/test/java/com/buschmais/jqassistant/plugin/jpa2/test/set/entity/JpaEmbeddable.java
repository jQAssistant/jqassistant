package com.buschmais.jqassistant.plugin.jpa2.test.set.entity;

import javax.persistence.Embeddable;

@Embeddable
public class JpaEmbeddable {

    private int intValue;

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }
}
