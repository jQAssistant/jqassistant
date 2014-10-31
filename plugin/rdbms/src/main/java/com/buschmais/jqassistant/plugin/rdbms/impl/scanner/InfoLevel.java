package com.buschmais.jqassistant.plugin.rdbms.impl.scanner;

import schemacrawler.schemacrawler.SchemaInfoLevel;

/**
 * Defines the info levels for schema retrieval.
 */
public enum InfoLevel {

    Standard {
        @Override
        public SchemaInfoLevel getSchemaInfoLevel() {
            return SchemaInfoLevel.standard();
        }
    },
    Minimum() {
        @Override
        public SchemaInfoLevel getSchemaInfoLevel() {
            return SchemaInfoLevel.minimum();
        }
    },
    Maximum {
        @Override
        public SchemaInfoLevel getSchemaInfoLevel() {
            return SchemaInfoLevel.maximum();
        }
    },
    Detailed {
        @Override
        public SchemaInfoLevel getSchemaInfoLevel() {
            return SchemaInfoLevel.detailed();
        }
    };

    public abstract SchemaInfoLevel getSchemaInfoLevel();
}
