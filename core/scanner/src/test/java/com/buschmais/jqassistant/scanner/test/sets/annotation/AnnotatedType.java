package com.buschmais.jqassistant.scanner.test.sets.annotation;

/**
 * Created with IntelliJ IDEA.
 * User: Dirk Mahler
 * Date: 13.07.13
 * Time: 16:52
 * To change this template use File | Settings | File Templates.
 */
@Annotation("class")
public class AnnotatedType {

    @Annotation("field")
    private String annotatedField;

    @Annotation("method")
    public void annotatedMethod(@Annotation("parameter") String parameter) {
		@Annotation("localField")
		String annotatedLocalField;
    }

}
