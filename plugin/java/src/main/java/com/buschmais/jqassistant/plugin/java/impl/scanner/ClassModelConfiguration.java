package com.buschmais.jqassistant.plugin.java.impl.scanner;

public class ClassModelConfiguration {

    private boolean typeDependsOnWeight = true;

    public boolean isTypeDependsOnWeight() {
        return typeDependsOnWeight;
    }

    public static class Builder {

        private ClassModelConfiguration instance = new ClassModelConfiguration();

        public static Builder newConfiguration() {
            return new Builder();
        }

        public Builder typeDependsOnWeight(boolean enabled) {
            instance.typeDependsOnWeight = enabled;
            return this;
        }

        public ClassModelConfiguration build() {
            return instance;
        }

    }
}
