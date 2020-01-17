package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.constructor.ImplicitDefaultConstructor;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.constructor.OverloadedConstructor;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.constructorDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

/**
 * Contains test which verify correct scanning of constructors.
 */
public class ConstructorIT extends AbstractJavaPluginIT {

    /**
     * Verifies scanning of {@link ImplicitDefaultConstructor}.
     *
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void implicitDefaultConstructor() throws NoSuchMethodException {
        scanClasses(ImplicitDefaultConstructor.class);
        store.beginTransaction();
        assertThat(query("MATCH (c:Method:Constructor) RETURN c").getColumn("c"), hasItem(constructorDescriptor(ImplicitDefaultConstructor.class)));
        store.commitTransaction();
    }

    /**
     * Verifies scanning of {@link OverloadedConstructor}.
     *
     * @throws NoSuchMethodException
     *             If the test fails.
     */

    @Test
    public void overloadedConstructors() throws NoSuchMethodException {
        scanClasses(OverloadedConstructor.class);
        store.beginTransaction();
        assertThat(query("MATCH (c:Method:Constructor) RETURN c").getColumn("c"),
                hasItems(constructorDescriptor(OverloadedConstructor.class), constructorDescriptor(OverloadedConstructor.class, String.class)));
        store.commitTransaction();
    }
}
