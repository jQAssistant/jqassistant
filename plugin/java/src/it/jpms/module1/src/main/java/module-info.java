module module1 {
    exports com.buschmais.jqassistant.plugin.java.it.jpms.module1.api;
    provides com.buschmais.jqassistant.plugin.java.it.jpms.module1.api.MyService with com.buschmais.jqassistant.plugin.java.it.jpms.module1.impl.MyServiceImpl;
}
