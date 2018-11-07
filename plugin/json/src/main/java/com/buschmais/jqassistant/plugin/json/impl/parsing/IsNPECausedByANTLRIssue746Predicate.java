package com.buschmais.jqassistant.plugin.json.impl.parsing;

public class IsNPECausedByANTLRIssue746Predicate {

    public boolean isNPECausedByANTLRIssue746Predicate(Throwable t) {
        boolean result = false;
        boolean isNPE = NullPointerException.class.isAssignableFrom(t.getClass());
        boolean stacktraceAvailable = t.getStackTrace().length > 0;

        if (stacktraceAvailable && isNPE) {
            StackTraceElement stackTraceElement = t.getStackTrace()[0];
            boolean correctClass = "org.antlr.v4.runtime.Parser".equals(stackTraceElement.getClassName());
            boolean correctMethod = "notifyErrorListeners".equals(stackTraceElement.getMethodName());

            result = correctClass && correctMethod;
        }

        return result;
    }
}
