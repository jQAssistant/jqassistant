package com.buschmais.jqassistant.core.model.api;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 24.06.13
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */
public class Result<T extends AbstractExecutable> {

    private T executable;

    private List<String> columnNames;

    private List<Map<String, Object>> rows;

    public Result(T executable, List<String> columnNames, List<Map<String, Object>> rows) {
        this.executable = executable;
        this.columnNames = columnNames;
        this.rows = rows;
    }

    public T getExecutable() {
        return executable;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }
}
