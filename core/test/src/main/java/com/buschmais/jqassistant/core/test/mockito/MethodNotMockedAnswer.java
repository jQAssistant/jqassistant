package com.buschmais.jqassistant.core.test.mockito;

import java.lang.reflect.Method;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class MethodNotMockedAnswer implements Answer {
    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        Method calledMethod = invocation.getMethod();
        String signature = calledMethod.toGenericString();

        throw new RuntimeException(signature + " is not mocked!");
    }
}
