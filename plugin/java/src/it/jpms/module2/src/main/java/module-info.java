open module java.it.jpms.module2 {

    requires java.it.jpms.module1;
    requires static transitive java.se;

    uses com.buschmais.jqassistant.plugin.java.it.jpms.module1.api.MyService;

}
