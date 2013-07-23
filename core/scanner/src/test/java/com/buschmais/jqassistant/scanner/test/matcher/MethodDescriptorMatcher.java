package com.buschmais.jqassistant.scanner.test.matcher;

import com.buschmais.jqassistant.store.api.model.MethodDescriptor;

import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * User: Dirk Mahler
 * Date: 14.07.13
 * Time: 17:10
 * To change this template use File | Settings | File Templates.
 */
public class MethodDescriptorMatcher extends AbstractDescriptorMatcher<MethodDescriptor> {

    public MethodDescriptorMatcher(String fqn) {
        super(MethodDescriptor.class, fqn);
    }

    public static MethodDescriptorMatcher methodDescriptor(Method method) {
        StringBuffer name = new StringBuffer();
        name.append(method.getDeclaringClass().getName());
        name.append('#');
        name.append(method.getReturnType().getCanonicalName());
        name.append(' ');
        name.append(method.getName());
        name.append('(');
        int parameterCount=0;
        for (Class<?> parameterType : method.getParameterTypes()) {
            if (parameterCount>0) {
                name.append(',');
            }
            name.append(parameterType.getCanonicalName());
            parameterCount++;
        }
        name.append(')');
        return new MethodDescriptorMatcher(name.toString());
    }

}
