package com.buschmais.jqassistant.plugin.java.it.jpms.module1.api;

public interface MyService {

    void run();

    interface InnerService {
        void run();
    }

}
