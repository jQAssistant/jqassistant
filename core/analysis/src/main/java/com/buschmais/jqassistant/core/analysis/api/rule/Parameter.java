package com.buschmais.jqassistant.core.analysis.api.rule;

public class Parameter {

    public enum Type {
        SHORT {
            @Override
            public Object parse(String value) {
                return Short.valueOf(value);
            }
        },
        INT {
            @Override
            public Object parse(String value) {
                return Integer.valueOf(value);
            }
        },
        LONG {
            @Override
            public Object parse(String value) {
                return Long.valueOf(value);
            }
        },
        DOUBLE {
            @Override
            public Object parse(String value) {
                return Double.valueOf(value);
            }
        },
        BOOLEAN {
            @Override
            public Object parse(String value) {
                return Boolean.valueOf(value);
            }
        },
        STRING {
            @Override
            public Object parse(String value) {
                return value;
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
