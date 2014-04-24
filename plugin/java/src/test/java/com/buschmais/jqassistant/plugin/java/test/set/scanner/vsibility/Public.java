package com.buschmais.jqassistant.plugin.java.test.set.scanner.vsibility;

public class Public {

    public int field;

    public void method() {
    }

    class Default {
        int field;

        void method() {
        }
    }

    protected class Protected {
        protected int field;

        protected void method() {
        }
    }

    @SuppressWarnings("unused")
    private class Private {
        private int field;

        private void method() {
        }
    }
}
