open module jpms.module2 {
    requires jpms.module1;
    requires static transitive java.se;

    uses com.buschmais.jqassistant.maven.it.jpms.module1.api.MyService;
    uses com.buschmais.jqassistant.maven.it.jpms.module1.api.MyService.InnerService;
}
