package com.buschmais.jqassistant.maven.it.jpms.module1.api;

public interface MyService {

    void run();

    interface InnerService {
        void run();
    }

}
