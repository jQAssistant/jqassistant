package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.constructorDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.constructor.ImplicitDefaultConstructor;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.constructor.OverloadedConstructor;

/**
 * Contains test which verify correct scanning of constructors.
 */
public class ConstructorIT extends AbstractJavaPluginIT {

    /**
     * Verifies scanning of {@link ImplicitDefaultConstructor}.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void implicitDefaultConstructor() throws IOException, NoSuchMethodException {
        scanClasses(ImplicitDefaultConstructor.class);
        store.beginTransaction();
        assertThat(query("MATCH (c:Method:Constructor) RETURN c").getColumn("c"), hasItem(constructorDescriptor(ImplicitDefaultConstructor.class)));
        store.commitTransaction();
    }

    /**
     * Verifies scanning of {@link OverloadedConstructor}.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */

    @Test
    public void overloadedConstructors() throws IOException, NoSuchMethodException {
        scanClasses(OverloadedConstructor.class);
        store.beginTransaction();
        assertThat(query("MATCH (c:Method:Constructor) RETURN c").getColumn("c"),
                allOf(hasItem(constructorDescriptor(OverloadedConstructor.class)), hasItem(constructorDescriptor(OverloadedConstructor.class, String.class))));
        store.commitTransaction();
    }
}
