package com.buschmais.jqassistant.core.analysis.api;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: dimahler
 * Date: 6/21/13
 * Time: 3:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class Concept {

    private Set<Concept> requiredConcepts = new HashSet<Concept>();

    private Query query;
}
