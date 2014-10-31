package com.buschmais.jqassistant.plugin.rdbms.api.model;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Column")
public interface ColumnDescriptor extends RdbmsDescriptor, NamedDescriptor, NullableDescriptor {

    @Relation("OF_COLUMN_TYPE")
    ColumnTypeDescriptor getColumnType();

    void setColumnType(ColumnTypeDescriptor columnType);

    boolean isAutoIncremented();

    void setAutoIncremented(boolean autoIncremented);

    int getSize();

    void setSize(int size);

    int getDecimalDigits();

    void setDecimalDigits(int decimalDigits);

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
}
