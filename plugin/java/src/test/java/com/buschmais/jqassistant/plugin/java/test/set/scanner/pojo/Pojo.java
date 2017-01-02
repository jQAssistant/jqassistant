package com.buschmais.jqassistant.plugin.java.test.set.scanner.pojo;

public class Pojo {

    private String stringValue;

    private int intValue;

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pojo pojo = (Pojo) o;

        if (intValue != pojo.intValue) return false;
        return stringValue != null ? stringValue.equals(pojo.stringValue) : pojo.stringValue == null;
    }

    @Override
    public int hashCode() {
        int result = stringValue != null ? stringValue.hashCode() : 0;
        result = 31 * result + intValue;
        return result;
    }
}
