module java.it.jpms.module1 {
    requires com.fasterxml.jackson.core;

    exports com.buschmais.jqassistant.plugin.java.it.jpms.module1.api;
    exports com.buschmais.jqassistant.plugin.java.it.jpms.module1.impl to java.it.jpms.module2;

    opens com.buschmais.jqassistant.plugin.java.it.jpms.module1.api;
    opens com.buschmais.jqassistant.plugin.java.it.jpms.module1.impl to java.it.jpms.module2;

    provides com.buschmais.jqassistant.plugin.java.it.jpms.module1.api.MyService with com.buschmais.jqassistant.plugin.java.it.jpms.module1.impl.MyServiceImpl;
}
