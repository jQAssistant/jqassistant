package com.buschmais.jqassistant.plugin.java.test.set.rules.java;

import java.util.stream.Stream;

public class LambdaMethod {

    public void withLambda() {
        Stream.of("Hello", "World").forEach(s -> System.out.println(s));
    }

    public void withoutLambda() {
        System.out.println("Hello World!");
    }
}
