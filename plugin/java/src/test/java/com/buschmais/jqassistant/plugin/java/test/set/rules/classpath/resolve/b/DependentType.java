package com.buschmais.jqassistant.plugin.java.test.set.rules.classpath.resolve.b;

import com.buschmais.jqassistant.plugin.java.test.set.rules.classpath.resolve.a.*;

@AnnotationType(classValue = ValueType.class, enumValue = EnumType.B)
public class DependentType extends ClassType implements InterfaceType {

    @AnnotationType
    private ClassType field;

    @AnnotationType
    public ClassType signature(@AnnotationType ClassType classType) throws ExceptionType {
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
