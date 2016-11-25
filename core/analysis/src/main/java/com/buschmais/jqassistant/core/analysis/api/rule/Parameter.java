package com.buschmais.jqassistant.core.analysis.api.rule;

public class Parameter {

    public enum Type {
        STRING {
            @Override
            public Object parse(String value) {
                return value;
            }
        },
        INT {
            @Override
            public Object parse(String value) {
                return Integer.valueOf(value);
            }
        };

        public abstract Object parse(String value);
    }

    private String name;

    private Type type;

    private Object defaultValue;

    public Parameter(String name, Type type, Object defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
