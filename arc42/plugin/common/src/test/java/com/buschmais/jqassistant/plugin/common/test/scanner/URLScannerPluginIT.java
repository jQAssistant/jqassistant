package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.net.MalformedURLException;
import java.net.URL;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class URLScannerPluginIT extends AbstractPluginIT {

    @Test
    void classPathURL() throws MalformedURLException {
        URL url = URLScannerPluginIT.class.getClassLoader()
            .getResource("java/lang/Object.class");
        verify(url.toString());
    }

    private void verify(String url) throws MalformedURLException {
        FileDescriptor fileDescriptor = getScanner().scan(new URL(url), url, DefaultScope.NONE);
        store.beginTransaction();
        assertThat(fileDescriptor).isNotNull();
        assertThat(fileDescriptor.getFileName()).isEqualTo(url);
        store.commitTransaction();
    }

}
