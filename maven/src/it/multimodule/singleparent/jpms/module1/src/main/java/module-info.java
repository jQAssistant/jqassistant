@Deprecated
module jpms.module1 {
    requires com.fasterxml.jackson.core;

    exports com.buschmais.jqassistant.maven.it.jpms.module1.api;
    exports com.buschmais.jqassistant.maven.it.jpms.module1.impl to jpms.module2;

    opens com.buschmais.jqassistant.maven.it.jpms.module1.api;
    opens com.buschmais.jqassistant.maven.it.jpms.module1.impl to jpms.module2;

    provides com.buschmais.jqassistant.maven.it.jpms.module1.api.MyService with com.buschmais.jqassistant.maven.it.jpms.module1.impl.MyServiceImpl;
    provides com.buschmais.jqassistant.maven.it.jpms.module1.api.MyService.InnerService with com.buschmais.jqassistant.maven.it.jpms.module1.impl.MyServiceImpl.InnerServiceImpl;
}
