package com.buschmais.jqassistant.plugin.java.it.jpms.module2.impl;

import com.buschmais.jqassistant.plugin.java.it.jpms.module1.api.MyService;

import java.util.ServiceLoader;

public class Main {

    public static void main(String... args) {
        MyService myService = ServiceLoader.load(MyService.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Service not found"));
        myService.run();
    }

}
