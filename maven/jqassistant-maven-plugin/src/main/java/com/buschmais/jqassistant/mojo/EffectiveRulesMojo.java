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

import java.util.Collection;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.buschmais.jqassistant.core.model.api.rules.Concept;
import com.buschmais.jqassistant.core.model.api.rules.Constraint;
import com.buschmais.jqassistant.core.model.api.rules.ConstraintGroup;
import com.buschmais.jqassistant.core.model.api.rules.RuleSet;

/**
 * A Mojo which lists all effective rules.
 *
 * @goal effective-rules
 * @requiresProject false
 */
public class EffectiveRulesMojo extends AbstractAnalysisMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        RuleSet ruleSet = readRules();
        List<ConstraintGroup> selectedConstraintGroups = getSelectedConstraintGroups(ruleSet);
        RuleSet targetRuleSet = new RuleSet();
        resolveConstraintGroups(selectedConstraintGroups, targetRuleSet);
        logRuleSet(targetRuleSet);
    }

    private void resolveConstraintGroups(Collection<ConstraintGroup> constraintGroups, RuleSet ruleSet) {
        for (ConstraintGroup constraintGroup : constraintGroups) {
            if (!ruleSet.getConstraintGroups().containsKey(constraintGroup.getId())) {
                ruleSet.getConstraintGroups().put(constraintGroup.getId(), constraintGroup);
                resolveConstraintGroups(constraintGroup.getConstraintGroups(), ruleSet);
                resolveConstraints(constraintGroup.getConstraints(), ruleSet);
            }
        }
    }

    private void resolveConstraints(Collection<Constraint> constraints, RuleSet ruleSet) {
        for (Constraint constraint : constraints) {
            if (!ruleSet.getConstraints().containsKey(constraint.getId())) {
                ruleSet.getConstraints().put(constraint.getId(), constraint);
                resolveConcepts(constraint.getRequiredConcepts(), ruleSet);
            }
        }
    }

    private void resolveConcepts(Collection<Concept> concepts, RuleSet ruleSet) {
        for (Concept concept : concepts) {
            if (!ruleSet.getConcepts().containsKey(concept.getId())) {
                ruleSet.getConcepts().put(concept.getId(), concept);
                resolveConcepts(concept.getRequiredConcepts(), ruleSet);
            }
        }
    }
}