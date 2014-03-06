package com.buschmais.jqassistant.plugin.java.test.set.rules.deprecated;

@SuppressWarnings("deprecation")
@Deprecated
public class DeprecatedType {

    @Deprecated
    private int value;

    @Deprecated
    public int getValue() {
        return value;
    }

    @Deprecated
    public void setValue(@Deprecated int value) {
        this.value = value;
    }
}
