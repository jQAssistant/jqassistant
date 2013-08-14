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

import com.buschmais.jqassistant.core.model.api.rules.RuleSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * A Mojo which lists all effective rules.
 *
 * @goal effective-rules
 * @phase validate
 * @requiresProject false
 */
public class EffectiveRulesMojo extends AbstractAnalysisMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        RuleSet targetRuleSet = resolveEffectiveRules();
        logRuleSet(targetRuleSet);
    }

}