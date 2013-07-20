package com.buschmais.jqassistant.core.analysis.api.model;

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

    private List<Map<String, Object>> rows;

    public Result(T executable, List<Map<String, Object>> rows) {
        this.executable = executable;
        this.rows = rows;
    }

    public T getExecutable() {
        return executable;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

}
