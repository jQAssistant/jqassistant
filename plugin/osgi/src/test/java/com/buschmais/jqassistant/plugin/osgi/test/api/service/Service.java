package com.buschmais.jqassistant.plugin.osgi.test.api.service;

import com.buschmais.jqassistant.plugin.osgi.test.api.data.Request;
import com.buschmais.jqassistant.plugin.osgi.test.api.data.Response;

/**
 * An example service interface.
 */
public interface Service {

    Response execute(Request request);

}
