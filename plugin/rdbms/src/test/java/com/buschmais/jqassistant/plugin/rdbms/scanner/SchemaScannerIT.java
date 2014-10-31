package com.buschmais.jqassistant.plugin.rdbms.scanner;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.hsqldb.jdbc.JDBCDriver;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.rdbms.api.model.*;

public class SchemaScannerIT extends AbstractPluginIT {

    public static final String PROPERTY_FILE = "jqassistant.plugin.rdbms-test.properties";

    @Test
    public void schema() throws IOException, SQLException, ClassNotFoundException {
        File testDb = new File("target/testdb");
        if (testDb.exists()) {
            testDb.delete();
        }
        Class.forName(JDBCDriver.class.getName());
        try (Connection c = DriverManager.getConnection("jdbc:hsqldb:file:target/testdb", "SA", "")) {
            try (PreparedStatement preparedStatement = c.prepareStatement("drop table if exists PERSON")) {
                preparedStatement.execute();
            }
            try (PreparedStatement preparedStatement = c
                    .prepareStatement("create table PERSON(a decimal(10,5), b decimal(5,2), c varchar(255) default 'defaultValue', primary key (a,b))")) {
                preparedStatement.execute();
            }
        }
        store.beginTransaction();
        File propertyFile = new File(getClassesDirectory(SchemaScannerIT.class), PROPERTY_FILE);
        ConnectionPropertiesDescriptor descriptor = getScanner().scan(propertyFile, propertyFile.getAbsolutePath(), JavaScope.CLASSPATH);
        store.commitTransaction();
        store.beginTransaction();
        assertThat(descriptor, notNullValue());
        // Verify schema
        List<SchemaDescriptor> schemas = descriptor.getSchemas();
        assertThat(schemas, hasSize(greaterThan(0)));
        SchemaDescriptor schemaDescriptor = schemas.get(0);
        assertThat(schemaDescriptor.getName(), notNullValue());
        // Verify person
        TableDescriptor person = getTable("PERSON");
        assertThat(person.getName(), equalTo("PERSON"));
        // Verify column A
        ColumnDescriptor a = getColumn("PERSON", "A");
        assertThat(a.getName(), equalTo("A"));
        assertThat(a.getSize(), equalTo(10));
        assertThat(a.getDecimalDigits(), equalTo(5));
        assertThat(a.isAutoIncremented(), equalTo(false));
        assertThat(a.isNullable(), equalTo(false));
        assertThat(a.getDefaultValue(), nullValue());
        assertThat(a.isPartOfPrimaryKey(), equalTo(true));
        assertThat(a.isPartOfIndex(), equalTo(true));
        assertThat(a.isPartOfForeignKey(), equalTo(false));
        assertThat(person.getColumns(), hasItem(a));
        // Verify column B
        ColumnDescriptor b = getColumn("PERSON", "B");
        assertThat(b.getName(), equalTo("B"));
        assertThat(b.getSize(), equalTo(5));
        assertThat(b.getDecimalDigits(), equalTo(2));
        assertThat(b.isAutoIncremented(), equalTo(false));
        assertThat(b.isNullable(), equalTo(false));
        assertThat(b.getDefaultValue(), nullValue());
        assertThat(b.isPartOfPrimaryKey(), equalTo(true));
        assertThat(b.isPartOfIndex(), equalTo(true));
        assertThat(b.isPartOfForeignKey(), equalTo(false));
        assertThat(person.getColumns(), hasItem(b));
        // Verify column C
        ColumnDescriptor c = getColumn("PERSON", "C");
        assertThat(c.getName(), equalTo("C"));
        assertThat(c.getSize(), equalTo(255));
        assertThat(c.getDecimalDigits(), equalTo(0));
        assertThat(c.isAutoIncremented(), equalTo(false));
        assertThat(c.isNullable(), equalTo(true));
        assertThat(c.getDefaultValue(), equalTo("'defaultValue'"));
        assertThat(c.isPartOfPrimaryKey(), equalTo(false));
        assertThat(c.isPartOfIndex(), equalTo(false));
        assertThat(c.isPartOfForeignKey(), equalTo(false));
        assertThat(person.getColumns(), hasItem(c));
        // Verify column type VARCHAR
        ColumnTypeDescriptor decimal = getColumnType("DECIMAL");
        assertThat(a.getColumnType(), is(decimal));
        assertThat(b.getColumnType(), is(decimal));
        assertThat(decimal.getMinimumScale(), equalTo(0));
        assertThat(decimal.getMaximumScale(), equalTo(32767));
        assertThat(decimal.getNumericPrecisionRadix(), equalTo(10));
        assertThat(decimal.getPrecision(), equalTo((long) Integer.MAX_VALUE));
        assertThat(decimal.isNullable(), equalTo(true));
        assertThat(decimal.isAutoIncrementable(), equalTo(true));
        assertThat(decimal.isCaseSensitive(), equalTo(false));
        assertThat(decimal.isFixedPrecisionScale(), equalTo(true));
        assertThat(decimal.isUnsigned(), equalTo(false));
        assertThat(decimal.isUserDefined(), equalTo(false));
        // Verfify column type VARCHAR
        ColumnTypeDescriptor varchar = getColumnType("VARCHAR");
        assertThat(c.getColumnType(), is(varchar));
        assertThat(varchar.getMinimumScale(), equalTo(0));
        assertThat(varchar.getMaximumScale(), equalTo(0));
        assertThat(varchar.getNumericPrecisionRadix(), equalTo(0));
        assertThat(varchar.getPrecision(), equalTo((long) Integer.MAX_VALUE));
        assertThat(varchar.isNullable(), equalTo(true));
        assertThat(varchar.isAutoIncrementable(), equalTo(false));
        assertThat(varchar.isCaseSensitive(), equalTo(true));
        assertThat(varchar.isFixedPrecisionScale(), equalTo(false));
        assertThat(varchar.isUnsigned(), equalTo(false));
        assertThat(varchar.isUserDefined(), equalTo(false));
        store.commitTransaction();
    }

    /**
     * Get a table.
     *
     * @param table
     *            The table name.
     * @return The table descriptor.
     */
    private TableDescriptor getTable(String table) {
        List<TableDescriptor> t = query("match (t:Rdbms:Table) where t.name={table} return t", MapBuilder.<String, Object> create("table", table).get())
                .getColumn("t");
        assertThat(t, hasSize(1));
        return t.get(0);
    }

    /**
     * Get a column.
     * 
     * @param table
     *            The table name.
     * @param column
     *            The column name.
     * @return The column descriptor.
     */
    private ColumnDescriptor getColumn(String table, String column) {
        List<ColumnDescriptor> c = query("match (t:Table)-[:HAS_COLUMN]->(c:Rdbms:Column) where t.name={table} and c.name={column} return c",
                MapBuilder.<String, Object> create("table", table).put("column", column).get()).getColumn("c");
        assertThat(c, hasSize(1));
        return c.get(0);
    }

    /**
     * Get a column type by its database type name.
     *
     * @param databaseType
     *            The name of the database type.
     * @return The table descriptor.
     */
    private ColumnTypeDescriptor getColumnType(String databaseType) {
        List<ColumnTypeDescriptor> t = query("match (t:Rdbms:ColumnType) where t.databaseType={databaseType} return t",
                MapBuilder.<String, Object> create("databaseType", databaseType).get()).getColumn("t");
        assertThat(t, hasSize(1));
        return t.get(0);
    }
}
