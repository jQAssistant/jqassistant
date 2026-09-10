package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.net.MalformedURLException;
import java.net.URL;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class URLScannerPluginIT extends AbstractPluginIT {

    public static final String CLASSPATH_RESOURCE = "/java/lang/Object.class";

    @Test
    void classPathURL() throws MalformedURLException {
        URL url = URLScannerPluginIT.class.getResource(CLASSPATH_RESOURCE);
        verify(url);
    }

    private void verify(URL url) throws MalformedURLException {
        FileDescriptor fileDescriptor = getScanner().scan(url, url.toString(), DefaultScope.NONE);
        store.beginTransaction();
        assertThat(fileDescriptor).isNotNull();
        assertThat(fileDescriptor.getFileName()).isEqualTo(url.getFile());
        assertThat(new URL(fileDescriptor.getPath())).isEqualTo(url);
        store.commitTransaction();
    }

}
