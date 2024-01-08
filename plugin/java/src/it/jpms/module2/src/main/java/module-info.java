module it.jpms.module2 {
    requires transitive it.jpms.module1;
    requires static java.se;
    uses com.buschmais.jqassistant.plugin.java.it.jpms.module1.api.MyService;
    opens com.buschmais.jqassistant.plugin.java.it.jpms.module2.impl;
}
