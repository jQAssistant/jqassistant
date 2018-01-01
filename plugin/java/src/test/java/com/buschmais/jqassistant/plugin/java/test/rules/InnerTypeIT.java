package com.buschmais.jqassistant.plugin.java.test.rules;

import java.util.List;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.innertype.OuterType;

import org.hamcrest.Matchers;
import org.junit.Test;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

public class InnerTypeIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:InnerType".
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void innerType() throws Exception {
        scanClasses(OuterType.class, OuterType.InnerClass.class, OuterType.InnerEnum.class, OuterType.InnerInterface.class, OuterType.InnerAnnotation.class);
        scanInnerClass(OuterType.class, "1");
        assertThat(applyConcept("java:InnerType").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(
                query("match (t:Inner:Type) return t").getColumn("t"),
                allOf(hasItem(typeDescriptor(OuterType.InnerClass.class)), hasItem(typeDescriptor(OuterType.InnerEnum.class)),
                        hasItem(typeDescriptor(OuterType.InnerInterface.class)), hasItem(typeDescriptor(OuterType.InnerAnnotation.class)),
                        hasItem(typeDescriptor(OuterType.class.getName() + "$1"))));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java:InnerType".
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void anonymousInnerTypes() throws Exception {
        scanClasses(OuterType.class, OuterType.InnerClass.class, OuterType.InnerEnum.class, OuterType.InnerInterface.class, OuterType.InnerAnnotation.class);
        scanInnerClass(OuterType.class, "1");
        assertThat(applyConcept("java:AnonymousInnerType").getStatus(), Matchers.equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> result = query("match (t:Anonymous:Inner:Type) return t").getColumn("t");
        assertThat(result.size(), equalTo(1));
        assertThat(result, allOf(hasItem(typeDescriptor(OuterType.class.getName() + "$1"))));
        store.commitTransaction();
    }
}
