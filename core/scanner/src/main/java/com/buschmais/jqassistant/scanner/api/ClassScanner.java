package com.buschmais.jqassistant.scanner.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Dirk Mahler
 * Date: 28.07.13
 * Time: 13:55
 * To change this template use File | Settings | File Templates.
 */
public interface ClassScanner {

    void scanArchive(File archive) throws IOException;

    void scanDirectory(File directory) throws IOException;

    void scanFile(File file) throws IOException;

    void scanClasses(Class<?>... classTypes) throws IOException;

    void scanInputStream(InputStream inputStream, String name) throws IOException;

    public abstract static class ScanListener {

        public void beforePackage() {
        }

        public void afterPackage() {
        }

        public void beforeClass() {
        }

        public void afterClass() {
        }
    }
}
