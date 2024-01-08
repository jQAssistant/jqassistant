module java.it.jpms.module2 {
    requires static java.se;
    requires transitive java.it.jpms.module1;

    uses com.buschmais.jqassistant.plugin.java.it.jpms.module1.api.MyService;
}
