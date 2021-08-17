package com.buschmais.jqassistant.core.rule.api.source;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Test;

public class FileRuleSourceTest extends AbstractRuleSourceTest {

    @Test
    protected List<RuleSource> getRuleSources() throws IOException {
        URL resource = FileRuleSource.class.getClassLoader().getResource("META-INF/jqassistant-rules");
        return FileRuleSource.getRuleSources(new File(resource.getPath()));
    }

}
