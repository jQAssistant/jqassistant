package com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.resolve.b;

import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.resolve.a.AnnotationType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.resolve.a.ClassType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.resolve.a.ExceptionType;

@AnnotationType
public class DependentType {

    @AnnotationType
    private int annotatedField;

    @AnnotationType
    public void annotatedMethod() {
    }

    public ClassType signature(ClassType classType) throws ExceptionType {
        return null;
    }

    public void fieldAccess(ClassType classType) {
        int foo = classType.foo;
        classType.foo = foo;
    }

    public void methodInvocation(ClassType classType) {
        classType.bar();
    }

}
