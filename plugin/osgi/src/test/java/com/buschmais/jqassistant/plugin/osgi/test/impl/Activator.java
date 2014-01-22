package com.buschmais.jqassistant.plugin.osgi.test.impl;

import com.buschmais.jqassistant.plugin.osgi.test.api.service.Service;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Hashtable;

/**
 * Example Activator.
 */
public class Activator implements BundleActivator {

    private ServiceRegistration<Service> service;

    @Override
    public void start(BundleContext context) throws Exception {
        service = context.registerService(Service.class, new ServiceImpl(), new Hashtable<String, Object>());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        service.unregister();
    }
}
