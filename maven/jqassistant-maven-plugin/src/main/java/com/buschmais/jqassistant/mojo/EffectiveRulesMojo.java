/**
 * Copyright (C) 2011 tdarby <tim.darby.uk@googlemail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.buschmais.jqassistant.mojo;

import com.buschmais.jqassistant.core.model.api.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @goal effective-rules
 * @requiresProject false
 */
public class EffectiveRulesMojo extends AbstractAnalysisMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(EffectiveRulesMojo.class);

    private Map<String, ConstraintGroup> resolvedConstraintGroups = new TreeMap<String, ConstraintGroup>();
    private Map<String, Constraint> resolvedConstraints = new TreeMap<String, Constraint>();
    private Map<String, Concept> resolvedConcepts = new TreeMap<String, Concept>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Map<String, ConstraintGroup> availableConstraintGroups = readRules();
        resolveConstraintGroups(availableConstraintGroups.values());
        LOGGER.info("Constraint groups [{}]", resolvedConstraintGroups.size());
        for (String id : resolvedConstraintGroups.keySet()) {
            LOGGER.info("  {}", id);
        }
        LOGGER.info("Constraints [{}]", resolvedConstraints.size());
        for (String id : resolvedConstraints.keySet()) {
            LOGGER.info("  {}", id);
        }
        LOGGER.info("Concepts [{}]", resolvedConcepts.size());
        for (String id : resolvedConcepts.keySet()) {
            LOGGER.info("  {}", id);
        }
    }

    private void resolveConstraintGroups(Collection<ConstraintGroup> constraintGroups) {
        for (ConstraintGroup constraintGroup : constraintGroups) {
            if (!resolvedConstraintGroups.containsKey(constraintGroup.getId())) {
                resolvedConstraintGroups.put(constraintGroup.getId(), constraintGroup);
                resolveConstraintGroups(constraintGroup.getConstraintGroups());
                resolveConstraints(constraintGroup.getConstraints());
            }
        }
    }

    private void resolveConstraints(Collection<Constraint> constraints) {
        for (Constraint constraint : constraints) {
            if (!resolvedConstraints.containsKey(constraint.getId())) {
                resolvedConstraints.put(constraint.getId(), constraint);
                resolveConcepts(constraint.getRequiredConcepts());
            }
        }
    }

    private void resolveConcepts(Collection<Concept> concepts) {
        for (Concept concept : concepts) {
            if (!resolvedConcepts.containsKey(concept.getId())) {
                resolvedConcepts.put(concept.getId(), concept);
                resolveConcepts(concept.getRequiredConcepts());
            }
        }
    }
}