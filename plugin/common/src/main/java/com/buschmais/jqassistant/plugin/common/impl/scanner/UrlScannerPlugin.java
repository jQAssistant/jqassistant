package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.bind.DatatypeConverter;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractResourceScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractFileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

import org.apache.commons.lang3.StringUtils;

/**
 * Scanner plugin which handles URLs as input, using standard Java mechanisms to handle protocols.
 */
public class UrlScannerPlugin extends AbstractResourceScannerPlugin<URL, FileDescriptor> {

    @Override
    public boolean accepts(URL item, String path, Scope scope) throws IOException {
        return scope.equals(DefaultScope.NONE);
    }

    @Override
    public FileDescriptor scan(final URL item, String path, Scope scope, Scanner scanner) throws IOException {
        try (FileResource fileResource = new BufferedFileResource(new AbstractFileResource() {
            @Override
            public InputStream createStream() throws IOException {
                URLConnection urlConnection = item.openConnection();
                if (item.getUserInfo() != null) {
                    String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(item.getUserInfo()
                        .getBytes());
                    urlConnection.setRequestProperty("Authorization", basicAuth);
                }
                return urlConnection.getInputStream();
            }
        })) {
            return scanner.scan(fileResource, getPath(item), scope);
        }
    }

    private String getPath(URL item) {
        // Rebuild the path without the username:password
        String protocol = item.getProtocol();
        String host = item.getHost();
        int port = item.getPort();
        String path = item.getPath();
        String query = item.getQuery();
        String ref = item.getRef();
        StringBuilder result = new StringBuilder();
        result.append(protocol)
            .append(":");
        if (StringUtils.isNotEmpty(host)) {
            result.append("//")
                .append(host);
        }
        if (port != -1) {
            result.append(":")
                .append(port);
        }
        if (path != null) {
            result.append(path);
        }
        if (query != null) {
            result.append("?")
                .append(query);
        }
        if (ref != null) {
            result.append("#")
                .append(ref);
        }
        return result.toString();
    }
}
