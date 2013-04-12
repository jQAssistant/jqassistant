package com.buschmais.jqassistant.store.api.model;

import java.util.List;
import java.util.Map;

public class QueryResult {

	private final List<String> columns;

	private final Iterable<Map<String, Object>> rows;

	public QueryResult(List<String> columns, Iterable<Map<String, Object>> rows) {
		super();
		this.columns = columns;
		this.rows = rows;
	}

	public List<String> getColumns() {
		return columns;
	}

	public Iterable<Map<String, Object>> getRows() {
		return rows;
	}
}
