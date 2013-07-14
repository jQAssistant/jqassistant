package com.buschmais.jqassistant.scanner.test.sets.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: Dirk Mahler
 * Date: 13.07.13
 * Time: 16:53
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Annotation {

    String value();

}
