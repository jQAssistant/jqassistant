package com.buschmais.jqassistant.plugin.rdbms.scanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.hsqldb.jdbc.JDBCDriver;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.rdbms.api.model.ConnectionPropertiesDescriptor;
import com.buschmais.jqassistant.plugin.rdbms.api.model.SchemaDescriptor;

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
            try (PreparedStatement preparedStatement = c.prepareStatement("create table PERSON(a integer, b varchar(255))")) {
                preparedStatement.execute();
            }
            try (PreparedStatement preparedStatement = c.prepareStatement("select * from PERSON")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    assertThat(resultSet.next(), equalTo(false));
                }
            }
        }
        store.beginTransaction();
        File propertyFile = new File(getClassesDirectory(SchemaScannerIT.class), PROPERTY_FILE);
        ConnectionPropertiesDescriptor descriptor = getScanner().scan(propertyFile, propertyFile.getAbsolutePath(), JavaScope.CLASSPATH);
        store.commitTransaction();
        store.beginTransaction();
        assertThat(descriptor, notNullValue());
        List<SchemaDescriptor> schemas = descriptor.getSchemas();
        assertThat(schemas, hasSize(greaterThan(0)));
        List<Object> c = query("match (t:Table)-[:HAS_COLUMN]->(c:Column) where t.name='PERSON' return c").getColumn("c");
        assertThat(c, hasSize(2));
        store.commitTransaction();

    }
}
