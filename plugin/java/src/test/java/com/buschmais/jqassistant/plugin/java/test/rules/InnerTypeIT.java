package com.buschmais.jqassistant.plugin.java.test.rules;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.innertype.OuterType;

public class InnerTypeIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:InnerType".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void innerType() throws IOException, AnalysisException, ClassNotFoundException {
        scanClasses(OuterType.class, OuterType.InnerClass.class, OuterType.InnerEnum.class, OuterType.InnerInterface.class, OuterType.InnerAnnotation.class);
        scanInnerClass(OuterType.class, "1");
        applyConcept("java:InnerType");
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
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void anonymousInnerTypes() throws IOException, AnalysisException, ClassNotFoundException {
        scanClasses(OuterType.class, OuterType.InnerClass.class, OuterType.InnerEnum.class, OuterType.InnerInterface.class, OuterType.InnerAnnotation.class);
        scanInnerClass(OuterType.class, "1");
        applyConcept("java:AnonymousInnerType");
        store.beginTransaction();
        List<Object> result = query("match (t:Anonymous:Inner:Type) return t").getColumn("t");
        assertThat(result.size(), equalTo(1));
        assertThat(result, allOf(hasItem(typeDescriptor(OuterType.class.getName() + "$1"))));
        store.commitTransaction();
    }
}
