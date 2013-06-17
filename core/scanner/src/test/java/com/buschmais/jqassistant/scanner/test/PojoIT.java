package com.buschmais.jqassistant.scanner.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.buschmais.jqassistant.scanner.test.sets.pojo.Pojo;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.QueryResult;

public class PojoIT extends AbstractScannerIT {

	@Test
	public void attributes() throws IOException {
		store.beginTransaction();
		scanner.scanClass(Pojo.class);
		store.endTransaction();
		String query = "MATCH (c:CLASS) RETURN c as class";
		Map<String, Object> parameters = Collections.emptyMap();
		QueryResult result = store.executeQuery(query, parameters);
		List<String> columns = result.getColumns();
		assertEquals(1, columns.size());
		assertEquals("class", columns.get(0));
		for (Map<String, Object> row : result.getRows()) {
			assertThat(row.keySet(), everyItem(equalTo("class")));
			assertThat(row.values(),
					everyItem(instanceOf(ClassDescriptor.class)));
		}
	}
}
