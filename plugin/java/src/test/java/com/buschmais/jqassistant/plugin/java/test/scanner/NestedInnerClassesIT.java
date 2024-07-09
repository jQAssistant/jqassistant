package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.innerclass.NestedInnerClasses;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Contains test on relations between outer and inner classes.
 */
class NestedInnerClassesIT extends AbstractJavaPluginIT {

    /**
     * Scans an outer class.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    void nestedInnerClasses() throws IOException {
        scanClasses(NestedInnerClasses.class, NestedInnerClasses.FirstLevel.class, NestedInnerClasses.FirstLevel.SecondLevel.class);
        store.beginTransaction();
        List<Map<String, Object>> rows = query("MATCH (t1:Type)-[:DECLARES]->(t2:Type)-[:DECLARES]->(t3:Type) RETURN t1, t2, t3").getRows();
        assertThat(rows.size()).isEqualTo(1);
        Map<String, Object> row = rows.get(0);
        assertThat(row.get("t1"), (Matcher<? super Object>) typeDescriptor(NestedInnerClasses.class));
        assertThat(row.get("t2"), (Matcher<? super Object>) typeDescriptor(NestedInnerClasses.FirstLevel.class));
        assertThat(row.get("t3"), (Matcher<? super Object>) typeDescriptor(NestedInnerClasses.FirstLevel.SecondLevel.class));
        store.commitTransaction();
    }
}
