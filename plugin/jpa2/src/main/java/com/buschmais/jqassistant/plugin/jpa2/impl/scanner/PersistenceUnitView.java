package com.buschmais.jqassistant.plugin.jpa2.impl.scanner;

import com.sun.java.xml.ns.persistence.Persistence;

import java.util.List;
import java.util.Properties;

public class PersistenceUnitView {
    private Persistence.PersistenceUnit unitV20;
    private org.jcp.xmlns.xml.ns.persistence.Persistence.PersistenceUnit unitV21;

    public PersistenceUnitView(Persistence.PersistenceUnit unit) {
        unitV20 = unit;
    }

    public PersistenceUnitView(org.jcp.xmlns.xml.ns.persistence.Persistence.PersistenceUnit unit) {
        unitV21 = unit;
    }

    public String getName() {
        return null != unitV20 ? unitV20.getName() : unitV21.getName();
    }

    public String getTransactionType() {
        return (null != unitV20) ? gn(unitV20.getTransactionType()) : gn(unitV21.getTransactionType());
    }

    public String getDescription() {
        return (null != unitV20) ? unitV20.getDescription() : unitV21.getDescription();
    }

    public String getJtaDataSource() {
        return (null != unitV20) ? unitV20.getJtaDataSource() : unitV21.getJtaDataSource();
    }

    public String getNonJtaDataSource() {
        return (null != unitV20) ? unitV20.getNonJtaDataSource() : unitV21.getNonJtaDataSource();
    }

    public String getProvider() {
        return (null != unitV20) ? unitV20.getProvider() : unitV21.getProvider();
    }

    public String getValidationMode() {
        return (null != unitV20) ? gn(unitV20.getValidationMode()) : gn(unitV21.getValidationMode());
    }

    public String getSharedCacheMode() {
        return (null != unitV20) ? gn(unitV20.getSharedCacheMode()) : gn(unitV21.getSharedCacheMode());
    }

    private static String gn(Enum<?> e) {
        return null != e ? e.name() : null;
    }

    public List<String> getClazz() {
        List<String> clazzes = null;

        if (null != unitV20) {
            clazzes = unitV20.getClazz();
        } else {
            clazzes = unitV21.getClazz();
        }

        return clazzes;
    }

    public Properties getProperties() {
        Properties properties = new Properties();

        if (null != unitV20) {
            if (unitV20.getProperties() != null) {
                for (Persistence.PersistenceUnit.Properties.Property property : unitV20.getProperties().getProperty()) {
                    properties.put(property.getName(), property.getValue());
                }
            }
        } else {
            if (unitV21.getProperties() != null) {
                for (org.jcp.xmlns.xml.ns.persistence.Persistence.PersistenceUnit.Properties.Property property : unitV21.getProperties().getProperty()) {
                    properties.put(property.getName(), property.getValue());
                }
            }
        }

        return properties;
    }
}
