package com.buschmais.jqassistant.plugin.cdi.test.set.beans.interceptor;

import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@CustomBinding
public class CustomInterceptor {

    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocationContext) throws Exception {
        return invocationContext.proceed();
    }

    @AroundTimeout
    public Object aroundTimeout(InvocationContext invocationContext) throws Exception {
        return invocationContext.proceed();
    }

    @AroundConstruct
    public Object aroundTConstruct(InvocationContext invocationContext) throws Exception {
        return invocationContext.proceed();
    }
}
