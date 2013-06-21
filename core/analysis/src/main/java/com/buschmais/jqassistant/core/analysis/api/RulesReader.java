package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.analysis.api.model.ConstraintGroup;

import javax.xml.transform.Source;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 21.06.13
 * Time: 21:21
 * To change this template use File | Settings | File Templates.
 */
public interface RulesReader {

    public Map<String, ConstraintGroup> read(List<Source> sources);

}
