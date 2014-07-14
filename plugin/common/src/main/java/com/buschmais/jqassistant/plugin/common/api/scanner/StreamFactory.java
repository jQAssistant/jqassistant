package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.io.IOException;
import java.io.InputStream;

public interface StreamFactory {

    InputStream createStream() throws IOException;
}
