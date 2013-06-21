package com.buschmais.jqassistant.core.analysis.api;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: dimahler
 * Date: 6/21/13
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConstraintGroup {

    private String id;

    private Set<Constraint> constraints=new HashSet<Constraint>();

    private Set<ConstraintGroup> constraintGroups = new HashSet<ConstraintGroup>();
}
