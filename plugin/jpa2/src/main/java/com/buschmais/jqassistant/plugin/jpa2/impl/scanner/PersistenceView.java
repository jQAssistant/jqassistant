package com.buschmais.jqassistant.plugin.jpa2.impl.scanner;

import com.sun.java.xml.ns.persistence.Persistence;

import java.util.ArrayList;
import java.util.List;

public class PersistenceView {
    private Persistence persistenceV20;
    private org.jcp.xmlns.xml.ns.persistence.Persistence persistenceV21;

    public PersistenceView(Persistence persistenceV20) {
        this.persistenceV20 = persistenceV20;
    }

    public PersistenceView(org.jcp.xmlns.xml.ns.persistence.Persistence persistenceV21) {
        this.persistenceV21 = persistenceV21;
    }

    public List<PersistenceUnitView> getPersistenceUnits() {
        int listSize = persistenceV20 != null ? persistenceV20.getPersistenceUnit().size()
                : persistenceV21.getPersistenceUnit().size();

        List<PersistenceUnitView> units = new ArrayList<>(listSize);

        if (persistenceV20 != null) {
            List<Persistence.PersistenceUnit> pusV20 = persistenceV20.getPersistenceUnit();

            for (Persistence.PersistenceUnit unit : pusV20) {
                units.add(new PersistenceUnitView(unit));
            }
        } else if (persistenceV21 != null) {
            List<org.jcp.xmlns.xml.ns.persistence.Persistence.PersistenceUnit> pusV21 = persistenceV21.getPersistenceUnit();

            for (org.jcp.xmlns.xml.ns.persistence.Persistence.PersistenceUnit unit : pusV21) {
                units.add(new PersistenceUnitView(unit));
            }

        }

        return units;
    }

    public String getVersion() {
        return persistenceV20 != null ? persistenceV20.getVersion() : persistenceV21.getVersion();
    }
}
