package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.model.CatchesDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CatchesIT extends AbstractJavaPluginIT {

    @Test
    void methodCachesException() {
        scanClasses(TestClass.class);
        store.beginTransaction();
        List<CatchesDescriptor> catches = query("MATCH (m:Method)-[c:CATCHES]->(:Type) RETURN c").getColumn("c");
        assertThat(catches).hasSize(1);
        CatchesDescriptor catchesDescriptor = catches.get(0);
        assertThat(catchesDescriptor.getLastLineNumber()).isEqualTo(catchesDescriptor.getFirstLineNumber() + 2);
        store.commitTransaction();
    }

    private static class TestClass {

        public void foo() {
            System.out.println("before try-catch");
            try {
                System.out.println("within try-catch");
                throw new IllegalStateException("illegal state");
            } catch (RuntimeException e) {
                e.printStackTrace();
            } finally {
                System.out.println("finally");
            }
            System.out.println("after try-catch");
        }

    }

}
