package com.buschmais.jqassistant.plugin.rdbms.api.model;

import com.buschmais.xo.neo4j.api.annotation.Indexed;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("ColumnType")
public interface ColumnTypeDescriptor extends RdbmsDescriptor, NullableDescriptor {

    @Indexed
    String getDatabaseType();

    void setDatabaseType(String databaseType);

    boolean isAutoIncrementable();

    void setAutoIncrementable(boolean autoIncrementable);

    long getPrecision();

    void setPrecision(long precision);

    int getMinimumScale();

    void setMinimumScale(int minimumScale);

    int getMaximumScale();

    void setMaximumScale(int maximumScale);

    boolean isCaseSensitive();

    void setCaseSensitive(boolean caseSensitive);

    boolean isFixedPrecisionScale();

    void setFixedPrecisionScale(boolean fixedPrecisionScale);

    int getNumericPrecisionRadix();

    void setNumericPrecisionRadix(int numPrecisionRadix);

    boolean isUnsigned();

    void setUnsigned(boolean unsigned);

    boolean isUserDefined();

    void setUserDefined(boolean userDefined);
}
