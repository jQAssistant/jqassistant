package com.buschmais.jqassistant.plugin.osgi.test.impl.b;

import com.buschmais.jqassistant.plugin.osgi.test.impl.a.UsedPublicClass;

public class UnusedPublicClass {

    private UsedPublicClass usedPublicClass;

    public UnusedPublicClass(UsedPublicClass usedPublicClass) {
        this.usedPublicClass = usedPublicClass;
    }

    public UsedPublicClass getUsedPublicClass() {
        return usedPublicClass;
    }
}
