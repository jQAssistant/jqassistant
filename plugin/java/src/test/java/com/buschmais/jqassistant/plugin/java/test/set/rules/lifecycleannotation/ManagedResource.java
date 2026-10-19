package com.buschmais.jqassistant.plugin.java.test.set.rules.lifecycleannotation;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class ManagedResource {

    public static class JavaEE {
        @PostConstruct
        public void postConstruct() {
        }

        @PreDestroy
        public void preDestroy() {
        }
    }

    public static class JakartaEE {
        @jakarta.annotation.PostConstruct
        public void postConstruct() {
        }

        @jakarta.annotation.PreDestroy
        public void preDestroy() {
        }
    }
}
