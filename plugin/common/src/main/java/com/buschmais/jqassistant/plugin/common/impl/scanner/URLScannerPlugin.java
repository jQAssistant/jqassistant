package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.URLFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractFileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractResourceScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractVirtualFileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.xo.api.Query;

import org.apache.commons.lang3.StringUtils;

/**
 * Scanner plugin which handles URLs as input, using standard Java mechanisms to handle protocols.
 */
public class URLScannerPlugin extends AbstractResourceScannerPlugin<URL, FileDescriptor> {

    @Override
    public boolean accepts(URL item, String path, Scope scope) throws IOException {
        return scope.equals(DefaultScope.NONE);
    }

    @Override
    public FileDescriptor scan(final URL url, String path, Scope scope, Scanner scanner) throws IOException {
        try (FileResource fileResource = new BufferedFileResource(new AbstractVirtualFileResource() {
            @Override
            public InputStream createStream() throws IOException {
                URLConnection urlConnection = url.openConnection();
                if (url.getUserInfo() != null) {
                    String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(url.getUserInfo()
                        .getBytes());
                    urlConnection.setRequestProperty("Authorization", basicAuth);
                }
                return urlConnection.getInputStream();
            }

            @Override
            protected String getRelativePath() throws IOException {
                URI uri;
                try {
                    uri = new URI(path);
                } catch (URISyntaxException e) {
                    throw new IOException("Cannot create URI from " + path, e);
                }
                String uriPath = uri.getPath();
                return uriPath != null ? uriPath : uri.getSchemeSpecificPart();
            }
        })) {
            URLFileResolver fileResolver = new URLFileResolver(getPath(url));
            ScannerContext scannerContext = scanner.getContext();
            scannerContext.push(FileResolver.class, fileResolver);
            try {
                FileDescriptor fileDescriptor = scanner.scan(fileResource, url.getFile(), scope);
                return scannerContext.getStore()
                    .addDescriptorType(fileDescriptor, URLFileDescriptor.class);
            } finally {
                scannerContext.pop(FileResolver.class);
            }
        }
    }

    private String getPath(URL url) {
        // Rebuild the path without the username:password
        String protocol = url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();
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
        return result.toString();
    }

    private static class URLFileResolver extends AbstractFileResolver {

        public URLFileResolver(String path) {
            super(path, URLFileResolver.class.getName());
        }

        @Override
        public <D extends FileDescriptor> D require(String requiredFileName, String containedFileName, Class<D> type, ScannerContext context) {
            return resolve(requiredFileName, type, false, context);
        }

        @Override
        public <D extends FileDescriptor> D match(String containedFileName, Class<D> type, ScannerContext context) {
            return resolve(containedFileName, type, true, context);
        }

        private <D extends FileDescriptor> D resolve(String requiredFileName, Class<D> type, boolean isMatch, ScannerContext context) {
            D fileDescriptor = getOrCreateAs(requiredFileName, type, fileName -> {
                try (Query.Result<Query.Result.CompositeRowObject> result = context.getStore()
                    .executeQuery("MATCH (file:File:URL) WHERE file.path=$url RETURN file", Map.of("url", getPath() + fileName))) {
                    return result.hasResult() ?
                        result.getSingleResult()
                            .get("file", FileDescriptor.class) :
                        null;
                }
            }, isMatch, context);
            return fileDescriptor.as(type);
        }
    }
}
