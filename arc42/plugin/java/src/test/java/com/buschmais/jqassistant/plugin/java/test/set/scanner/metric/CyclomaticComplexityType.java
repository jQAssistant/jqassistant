package com.buschmais.jqassistant.plugin.java.test.set.scanner.metric;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Defines a class for verifying calculation of cyclomatic complexity metrics of
 * the class scanner.
 */
public class CyclomaticComplexityType {

    public CyclomaticComplexityType() {
    }

    public void ifStatement(boolean value) {
        if (value) {
            System.out.println("if");
        } else {
            System.out.println("else");
        }
    }

    public void caseStatement(int value) {
        switch (value) {
        case 0:
            System.out.println("0");
            break;
        case 1:
            System.out.println("1");
            break;
        default:
            System.out.println("default");
        }
    }

    public void tryCatch() {
        System.out.println("before");
        try {
            System.out.println("try");
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("finally");
        }
        System.out.println("after");
    }

    public void nestedTryCatch() {
        System.out.println("before");
        try {
            try {
                System.out.println("try");
            } catch (IllegalStateException e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println("finally");
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
        System.out.println("after");
    }

    public void tryWithResources() {
        System.out.println("before");
        try (InputStream is = new FileInputStream("test")) {
            System.out.println("try");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("after");
    }

}
