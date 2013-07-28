package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.model.api.ConstraintGroup;

import javax.xml.transform.Source;
import java.util.List;
import java.util.Map;

/**
 * Defines the interface of the rules reader.
 */
public interface RulesReader {

    /**
     * Reads the given source and returns a map containing the resolved constraint groups identified by their name.
     *
     * @param sources The sources to be read.
     * @return The map of constraint groups.
     */
    public Map<String, ConstraintGroup> read(List<Source> sources);

}
