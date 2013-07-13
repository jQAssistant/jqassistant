package com.buschmais.jqassistant.core.analysis.test;


import com.buschmais.jqassistant.core.analysis.api.RulesReader;
import com.buschmais.jqassistant.core.analysis.api.model.ConstraintGroup;
import com.buschmais.jqassistant.core.analysis.impl.RulesReaderImpl;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 21.06.13
 * Time: 21:45
 * To change this template use File | Settings | File Templates.
 */
public class RulesReaderTest {

    private static Map<String, ConstraintGroup> constraintGroupMap;

    @BeforeClass
    public static void readRules() {
        InputStream is = RulesReaderTest.class.getResourceAsStream("/jqassistant-rules.xml");
        List<Source> rules = new ArrayList<Source>();
        rules.add(new StreamSource(is));
        RulesReader reader = new RulesReaderImpl();
        constraintGroupMap = reader.read(rules);
    }

    @Test
    public void queryDefinition() {

    }
}
