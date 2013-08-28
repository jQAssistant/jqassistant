package com.buschmais.jqassistant.core.scanner.test;

import com.buschmais.jqassistant.core.scanner.test.set.constructor.ImplicitDefaultConstructor;
import com.buschmais.jqassistant.core.scanner.test.set.constructor.OverloadedConstructor;
import org.junit.Test;

import java.io.IOException;

import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.MethodDescriptorMatcher.constructorDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Contains test which verify correct scanning of constructors.
 */
public class ConstructorIT extends AbstractScannerIT {

    /**
     * Verifies scanning of {@link ImplicitDefaultConstructor}.
     *
     * @throws java.io.IOException   If the test fails.
     * @throws NoSuchMethodException If the test fails.
     */
    @Test
    public void implicitDefaultConstructor() throws IOException, NoSuchMethodException {
        scanClasses(ImplicitDefaultConstructor.class);
        assertThat(query("MATCH (c:METHOD:CONSTRUCTOR) RETURN c").getColumns().get("c"), hasItem(constructorDescriptor(ImplicitDefaultConstructor.class)));
    }

    /**
     * Verifies scanning of {@link OverloadedConstructor}.
     *
     * @throws java.io.IOException   If the test fails.
     * @throws NoSuchMethodException If the test fails.
     */

    @Test
    public void overloadedConstructors() throws IOException, NoSuchMethodException {
        scanClasses(OverloadedConstructor.class);
        assertThat(query("MATCH (c:METHOD:CONSTRUCTOR) RETURN c").getColumns().get("c"), allOf(hasItem(constructorDescriptor(OverloadedConstructor.class)), hasItem(constructorDescriptor(OverloadedConstructor.class, String.class))));
    }
}
