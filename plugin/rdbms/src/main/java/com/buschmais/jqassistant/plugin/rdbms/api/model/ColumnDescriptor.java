package com.buschmais.jqassistant.plugin.rdbms.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Column")
public interface ColumnDescriptor extends RdbmsDescriptor, BaseColumnDescriptor {

    boolean isAutoIncremented();

    void setAutoIncremented(boolean autoIncremented);

    String getDefaultValue();

    void setDefaultValue(String defaultValue);

    boolean isGenerated();

    void setGenerated(boolean generated);

    boolean isPartOfIndex();

    void setPartOfIndex(boolean partOfIndex);

    boolean isPartOfPrimaryKey();

    void setPartOfPrimaryKey(boolean partOfPrimaryKey);

    boolean isPartOfForeignKey();

    void setPartOfForeignKey(boolean partOfForeignKey);

    PrimaryKeyOnColumnDescriptor getPrimaryKeyOnColumn();

    IndexOnColumnDescriptor getIndexOnColumn();

}
