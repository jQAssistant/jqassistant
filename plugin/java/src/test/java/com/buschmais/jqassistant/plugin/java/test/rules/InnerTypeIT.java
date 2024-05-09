package com.buschmais.jqassistant.plugin.java.test.rules;

import java.util.List;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.innertype.OuterType;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.assertj.TypeDescriptorCondition.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;

class InnerTypeIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:InnerType".
     */
    @Test
    void innerType() throws RuleException, ClassNotFoundException {
        scanClasses(OuterType.class, OuterType.InnerClass.class, OuterType.InnerEnum.class, OuterType.InnerInterface.class, OuterType.InnerAnnotation.class);
        scanInnerClass(OuterType.class, "1");
        assertThat(applyConcept("java:InnerType").getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        assertThat(query("match (t:Inner:Type) return t").<TypeDescriptor>getColumn("t")).haveExactly(1, typeDescriptor(OuterType.InnerClass.class))
            .haveExactly(1, typeDescriptor(OuterType.InnerEnum.class))
            .haveExactly(1, typeDescriptor(OuterType.InnerInterface.class))
            .haveExactly(1, typeDescriptor(OuterType.InnerAnnotation.class))
            .haveExactly(1, typeDescriptor(OuterType.class.getName() + "$1"));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java:InnerType".
     */
    @Test
    void anonymousInnerTypes() throws RuleException, ClassNotFoundException {
        scanClasses(OuterType.class, OuterType.InnerClass.class, OuterType.InnerEnum.class, OuterType.InnerInterface.class, OuterType.InnerAnnotation.class);
        scanInnerClass(OuterType.class, "1");
        assertThat(applyConcept("java:AnonymousInnerType").getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        List<TypeDescriptor> result = query("match (t:Anonymous:Inner:Type) return t").getColumn("t");
        assertThat(result).hasSize(1)
            .haveExactly(1, typeDescriptor(OuterType.class.getName() + "$1"));
        store.commitTransaction();
    }
}
