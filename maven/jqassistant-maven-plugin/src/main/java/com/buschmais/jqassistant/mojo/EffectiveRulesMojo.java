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

import com.buschmais.jqassistant.core.model.api.rules.AnalysisGroup;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.buschmais.jqassistant.core.model.api.rules.Concept;
import com.buschmais.jqassistant.core.model.api.rules.Constraint;
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
        List<AnalysisGroup> selectedAnalysisGroups = getSelectedAnalysisGroups(ruleSet);
        RuleSet targetRuleSet = new RuleSet();
        resolveAnalysisGroups(selectedAnalysisGroups, targetRuleSet);
        logRuleSet(targetRuleSet);
    }

    private void resolveAnalysisGroups(Collection<AnalysisGroup> analysisGroups, RuleSet ruleSet) {
        for (AnalysisGroup analysisGroup : analysisGroups) {
            if (!ruleSet.getAnalysisGroups().containsKey(analysisGroup.getId())) {
                ruleSet.getAnalysisGroups().put(analysisGroup.getId(), analysisGroup);
                resolveAnalysisGroups(analysisGroup.getAnalysisGroups(), ruleSet);
                resolveConstraints(analysisGroup.getConstraints(), ruleSet);
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