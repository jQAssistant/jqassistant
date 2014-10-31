package com.buschmais.jqassistant.core.analysis.api.rule.source;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A rule source which is provided from an URL.
 */
public class UrlRuleSource extends RuleSource {

    private URL url;

    public UrlRuleSource(URL url) {
        this.url = url;
    }

    @Override
    public String getId() {
        return url.toExternalForm();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return url.openStream();
    }
}
