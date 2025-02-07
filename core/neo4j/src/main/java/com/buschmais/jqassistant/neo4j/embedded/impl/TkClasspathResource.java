package com.buschmais.jqassistant.neo4j.embedded.impl;

import java.io.InputStream;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.takes.HttpException;
import org.takes.rq.RqHref;
import org.takes.rs.RsWithBody;
import org.takes.tk.TkWrap;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

/**
 * HTTP request handler for classpath resources
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
final class TkClasspathResource extends TkWrap {

    public static final String STATIC_CONTENT_PREFIX = "/jqassistant";

    public TkClasspathResource(ClassLoader classLoader) {
        super(request -> {
            String path = new RqHref.Base(request).href()
                .path();
            String resource = resolve(path);
            final InputStream input = classLoader.getResourceAsStream(resource);
            if (input == null) {
                throw new HttpException(HTTP_NOT_FOUND, String.format("%s not found.", resource));
            }
            return new RsWithBody(input);
        });
    }

    static String resolve(String path) {
        if (path.endsWith("/")) {
            return resolve(path + "index.html");
        }
        if (path.startsWith(STATIC_CONTENT_PREFIX)) {
            return resolve("META-INF/jqassistant-static-content", path.substring(STATIC_CONTENT_PREFIX.length()));
        }
        return resolve("browser", path);
    }

    private static String resolve(String prefix, String path) {
        return String.format("%s%s", prefix, path);
    }

}
