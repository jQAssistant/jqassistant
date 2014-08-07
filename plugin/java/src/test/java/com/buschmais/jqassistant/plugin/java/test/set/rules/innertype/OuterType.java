package com.buschmais.jqassistant.plugin.java.test.set.rules.innertype;

import java.util.Comparator;

public class OuterType {

    public class InnerClass {
    }

    public enum InnerEnum {
    }

    public interface InnerInterface {
    }

    public @interface InnerAnnotation {
    }

    public void doSomething() {
        Comparator comparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };
    }
}
