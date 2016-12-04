package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.analysis.api.RuleException;

/**
 * Defines a rule parameter.
 */
public class Parameter {

    /**
     * Defines the supported parameter types.
     */
    public enum Type {
        CHAR {
            @Override
            protected Object parseString(String value) {
                return value.charAt(0);
            }
        },
        BYTE {
            @Override
            protected Object parseString(String value) {
                return Byte.valueOf(value);
            }
        },
        SHORT {
            @Override
            public Object parseString(String value) {
                return Short.valueOf(value);
            }
        },
        INT {
            @Override
            public Object parseString(String value) {
                return Integer.valueOf(value);
            }
        },
        LONG {
            @Override
            public Object parseString(String value) {
                return Long.valueOf(value);
            }
        },
        FLOAT {
            @Override
            public Object parseString(String value) {
                return Float.valueOf(value);
            }
        },
        DOUBLE {
            @Override
            public Object parseString(String value) {
                return Double.valueOf(value);
            }
        },
        BOOLEAN {
            @Override
            public Object parseString(String value) {
                return Boolean.valueOf(value);
            }
        },
        STRING {
            @Override
            public Object parseString(String value) {
                return value;
            }
        };

        protected abstract Object parseString(String value);

        public Object parse(String value) throws RuleException {
            try {
                return value != null ? parseString(value) : null;
            } catch (NumberFormatException e) {
                throw new RuleException("Cannot parse value " + value + " for type " + name());
            }
        };

    }

    private String name;

    private Type type;

    private Object defaultValue;

    /**
     * Constructor.
     * 
     * @param name
     *            The parameter name.
     * @param type
     *            The parameter type.
     * @param defaultValue
     *            The default value (optional).
     */
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
