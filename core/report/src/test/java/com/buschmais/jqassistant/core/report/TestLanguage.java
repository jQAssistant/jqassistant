package com.buschmais.jqassistant.core.report;

import com.buschmais.jqassistant.core.store.api.descriptor.Language;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A test language to be verified in the XML report.
 */
@Language("testLanguage")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestLanguage {

    String value();
}
