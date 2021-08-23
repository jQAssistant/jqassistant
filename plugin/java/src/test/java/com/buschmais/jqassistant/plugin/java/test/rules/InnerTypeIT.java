package com.buschmais.jqassistant.plugin.java.test.rules;

import java.util.List;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.innertype.OuterType;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

class InnerTypeIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:InnerType".
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    void innerType() throws RuleException, ClassNotFoundException {
        scanClasses(OuterType.class, OuterType.InnerClass.class, OuterType.InnerEnum.class, OuterType.InnerInterface.class, OuterType.InnerAnnotation.class);
        scanInnerClass(OuterType.class, "1");
        assertThat(applyConcept("java:InnerType").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("match (t:Inner:Type) return t").getColumn("t"),
                hasItems(typeDescriptor(OuterType.InnerClass.class), typeDescriptor(OuterType.InnerEnum.class), typeDescriptor(OuterType.InnerInterface.class),
                        typeDescriptor(OuterType.InnerAnnotation.class), typeDescriptor(OuterType.class.getName() + "$1")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java:InnerType".
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    void anonymousInnerTypes() throws RuleException, ClassNotFoundException {
        scanClasses(OuterType.class, OuterType.InnerClass.class, OuterType.InnerEnum.class, OuterType.InnerInterface.class, OuterType.InnerAnnotation.class);
        scanInnerClass(OuterType.class, "1");
        assertThat(applyConcept("java:AnonymousInnerType").getStatus(), Matchers.equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> result = query("match (t:Anonymous:Inner:Type) return t").getColumn("t");
        assertThat(result.size(), equalTo(1));
        assertThat(result, hasItem(typeDescriptor(OuterType.class.getName() + "$1")));
        store.commitTransaction();
    }
}
