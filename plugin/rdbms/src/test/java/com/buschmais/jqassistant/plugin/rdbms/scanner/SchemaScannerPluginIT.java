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
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.hsqldb.jdbc.JDBCDriver;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.rdbms.api.model.*;
import com.buschmais.jqassistant.plugin.rdbms.impl.scanner.SchemaScannerPlugin;

public class SchemaScannerPluginIT extends AbstractPluginIT {

    public static final String DEFAULT_FILE = "default";
    public static final String TABLE_PERSON = "PERSON";
    public static final String COLUMN_A = "A";
    public static final String COLUMN_B = "B";
    public static final String COLUMN_C = "C";
    public static final String COLUMNTYPE_DECIMAL = "DECIMAL";
    public static final String COLUMNTYPE_VARCHAR = "VARCHAR";
    public static final String SEQUENCE_PERSON_SEQ = "PERSON_SEQ";

    @Before
    public void createStructures() throws SQLException, ClassNotFoundException {
        Class.forName(JDBCDriver.class.getName());
        try (Connection c = DriverManager.getConnection("jdbc:hsqldb:file:target/testdb", "SA", "")) {
            execute(c, "drop table if exists PERSON");
            execute(c, "create table PERSON(a decimal(10,5), b decimal(5,2), c varchar(255) default 'defaultValue', primary key (a,b))");
            execute(c, "drop sequence if exists PERSON_SEQ");
            execute(c, "create sequence PERSON_SEQ minvalue 100 maxvalue 10000  start with 100 increment by 10 cycle");
        }
    }

    /**
     * Scan using infolevel minimum
     */
    @Test
    public void minimum() {
        scan("minimum");
        store.beginTransaction();
        assertThat(getTable(TABLE_PERSON), notNullValue());
        assertThat(getColumn(TABLE_PERSON, COLUMN_A), nullValue());
        store.commitTransaction();
    }

    /**
     * Scan using info level minimum
     */
    @Test
    public void noTables() {
        scan("notables");
        store.beginTransaction();
        assertThat(getTable(TABLE_PERSON), nullValue());
        store.commitTransaction();
    }

    @Test
    public void tablesAndColumns() {
        scan(DEFAULT_FILE);
        store.beginTransaction();
        // Verify person
        TableDescriptor person = getTable(TABLE_PERSON);
        assertThat(person.getName(), equalTo(TABLE_PERSON));
        // Verify column A
        ColumnDescriptor a = getColumn(TABLE_PERSON, COLUMN_A);
        assertThat(a.getName(), equalTo(COLUMN_A));
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
        ColumnDescriptor b = getColumn(TABLE_PERSON, COLUMN_B);
        assertThat(b.getName(), equalTo(COLUMN_B));
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
        ColumnDescriptor c = getColumn(TABLE_PERSON, COLUMN_C);
        assertThat(c.getName(), equalTo(COLUMN_C));
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
        ColumnTypeDescriptor decimal = getColumnType(COLUMNTYPE_DECIMAL);
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
        ColumnTypeDescriptor varchar = getColumnType(COLUMNTYPE_VARCHAR);
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

    @Test
    @Ignore("Need to investigate how schemacrawler needs to be configured to retrieve sequence information for hsqldb")
    public void sequences() throws IOException {
        scan(DEFAULT_FILE);
        store.beginTransaction();
        SequenceDesriptor sequence = getSequence(SEQUENCE_PERSON_SEQ);
        assertThat(sequence.getName(), equalTo(SEQUENCE_PERSON_SEQ));
        assertThat(sequence.getMinimumValue(), equalTo(BigInteger.valueOf(100)));
        assertThat(sequence.getMaximumValue(), equalTo(BigInteger.valueOf(10000)));
        assertThat(sequence.getIncrement(), equalTo(10l));
        assertThat(sequence.isCycle(), equalTo(true));
        store.commitTransaction();
    }

    /**
     * Scans the test tablesAndColumns.
     */
    private void scan(String name) {
        store.beginTransaction();
        String fileName = SchemaScannerPlugin.PLUGIN_NAME + "-" + name + SchemaScannerPlugin.PROPERTIES_SUFFIX;
        File propertyFile = new File(getClassesDirectory(SchemaScannerPluginIT.class), fileName);
        ConnectionPropertiesDescriptor descriptor = getScanner().scan(propertyFile, propertyFile.getAbsolutePath(), JavaScope.CLASSPATH);
        assertThat(descriptor, notNullValue());
        List<SchemaDescriptor> schemas = descriptor.getSchemas();
        assertThat(schemas, hasSize(greaterThan(0)));
        SchemaDescriptor schemaDescriptor = schemas.get(0);
        assertThat(schemaDescriptor.getName(), notNullValue());
        store.commitTransaction();
    }

    /**
     * Execute a DDL statement.
     *
     * @param connection
     *            The connection.
     * @param ddl
     *            The ddl.
     * @throws SQLException
     *             If execution fails.
     */
    private void execute(Connection connection, String ddl) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(ddl)) {
            preparedStatement.execute();
        }
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
        return t == null ? null : t.get(0);
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
        return c == null ? null : c.get(0);
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
        return t == null ? null : t.get(0);
    }

    /**
     * Get a sequence.
     *
     * @param sequence
     *            The sequence name.
     * @return The table descriptor.
     */
    private SequenceDesriptor getSequence(String sequence) {
        List<SequenceDesriptor> s = query("match (s:Rdbms:Sequence) where s.name={sequence} return s",
                MapBuilder.<String, Object> create("sequence", sequence).get()).getColumn("s");
        return s == null ? null : s.get(0);
    }

}
