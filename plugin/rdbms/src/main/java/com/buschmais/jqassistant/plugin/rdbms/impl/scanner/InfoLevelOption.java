package com.buschmais.jqassistant.plugin.rdbms.impl.scanner;

import schemacrawler.schemacrawler.SchemaInfoLevel;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;

/**
 * Defines the information levels of schema crawler.
 */
public enum InfoLevelOption {

    RetrieveAdditionalColumnAttributes {
        @Override
        public void set(SchemaInfoLevel level, boolean value) {
            level.setRetrieveAdditionalColumnAttributes(value);
        }
    },
    RetrieveAdditionalDatabaseInfo {
        @Override
        public void set(SchemaInfoLevel level, boolean value) {
            level.setRetrieveAdditionalDatabaseInfo(value);
        }
    },
    RetrieveAdditionalJdbcDriverInfo {
        @Override
        public void set(SchemaInfoLevel level, boolean value) {
            level.setRetrieveAdditionalJdbcDriverInfo(value);
        }
    },
    RetrieveAdditionalTableAttributes {
        @Override
        public void set(SchemaInfoLevel level, boolean value) {
            level.setRetrieveAdditionalTableAttributes(value);
        }
    },
    RetrieveForeignKeys {
        @Override
        public void set(SchemaInfoLevel level, boolean value) {
            level.setRetrieveForeignKeys(value);
        }
    },
    RetrieveIndices {
        @Override
        public void set(SchemaInfoLevel level, boolean value) {
            level.setRetrieveIndices(value);
        }
    },
    RetrieveIndexInformation {
        @Override
        public void set(SchemaInfoLevel level, boolean value) {
            level.setRetrieveIndexInformation(value);
        }
    },
    RetrieveRoutines {
        @Override
        public void set(SchemaInfoLevel level, boolean value) {
            level.setRetrieveRoutines(value);
        }
    },
    RetrieveRoutineColumns {
        @Override
        public void set(SchemaInfoLevel level, boolean value) {
            level.setRetrieveRoutineColumns(value);
        }
    },
    RetrieveRoutineInformation {
        @Override
        public void set(SchemaInfoLevel level, boolean value) {
            level.setRetrieveRoutineInformation(value);
        }
    },
    RetrieveSchemaCrawlerInfo {
        @Override
        public void set(SchemaInfoLevel level, boolean value) {
            level.setRetrieveSchemaCrawlerInfo(value);
        }
    },
    RetrieveSequenceInformation {
        @Override
        public void set(SchemaInfoLevel level, boolean value) {
            level.setRetrieveSequenceInformation(value);
        }
    },
    RetrieveTables {
        @Override
        public void set(SchemaInfoLevel level, boolean value) {
            level.setRetrieveTables(value);
        }
    },
    RetrieveTableColumns {
        @Override
        public void set(SchemaInfoLevel level, boolean value) {
            level.setRetrieveTableColumns(value);
        }
    },
    RetrieveTriggerInformation {
        @Override
        public void set(SchemaInfoLevel level, boolean value) {
            level.setRetrieveTriggerInformation(value);
        }
    },
    RetrieveViewInformation {
        @Override
        public void set(SchemaInfoLevel level, boolean value) {
            level.setRetrieveViewInformation(value);
        }
    };

    /**
     * Return the name of the property as it is expected in the configuration.
     * 
     * @return The property name.
     */
    String getPropertyName() {
        return UPPER_CAMEL.to(LOWER_UNDERSCORE, name());
    }

    /**
     * Set an option.
     * 
     * @param level
     *            The schema info level attribute.
     * @param value
     *            The value.
     */
    public abstract void set(SchemaInfoLevel level, boolean value);

}
