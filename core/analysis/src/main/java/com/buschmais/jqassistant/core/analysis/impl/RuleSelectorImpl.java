package com.buschmais.jqassistant.core.analysis.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.api.RuleSelector;
import com.buschmais.jqassistant.core.analysis.api.RuleSetResolverException;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;

/**
 * Implementation of the {@link RuleSelector}.
 */
public class RuleSelectorImpl implements RuleSelector {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleSelectorImpl.class);

	@Override
	public RuleSet getEffectiveRuleSet(RuleSet ruleSet, List<String> conceptNames, List<String> constraintNames, List<String> groupNames)
			throws RuleSetResolverException {
		RuleSet effectiveRuleSet = new RuleSet();
		// Use the default group if no group, constraint or concept is
		// specified.
		List<String> selectedGroupNames;
		if (CollectionUtils.isEmpty(groupNames) && CollectionUtils.isEmpty(constraintNames) && CollectionUtils.isEmpty(conceptNames)) {
			selectedGroupNames = Arrays.asList(new String[] { DEFAULT_GROUP });
		} else {
			selectedGroupNames = groupNames;
		}
		resolveConcepts(getSelectedConcepts(conceptNames, ruleSet), effectiveRuleSet);
		resolveConstraints(getSelectedConstraints(constraintNames, ruleSet), effectiveRuleSet);
		resolveGroups(getSelectedGroups(selectedGroupNames, ruleSet), effectiveRuleSet);
		return effectiveRuleSet;
	}

	/**
	 * Resolve the given selected groups names into the target rules set.
	 * 
	 * @param groups
	 *            The selected group names.
	 * @param targetRuleSet
	 *            The target rules set.
	 */
	private void resolveGroups(Collection<Group> groups, RuleSet targetRuleSet) {
		for (Group group : groups) {
			if (!targetRuleSet.getGroups().containsKey(group.getId())) {
				targetRuleSet.getGroups().put(group.getId(), group);
				resolveGroups(group.getGroups(), targetRuleSet);
				resolveConcepts(group.getConcepts(), targetRuleSet);
				resolveConstraints(group.getConstraints(), targetRuleSet);
			}
		}
	}

	/**
	 * Resolve the given selected constraint names into the target rules set.
	 * 
	 * @param constraints
	 *            The selected constraint names.
	 * @param targetRuleSet
	 *            The target rules set.
	 */
	private void resolveConstraints(Collection<Constraint> constraints, RuleSet targetRuleSet) {
		for (Constraint constraint : constraints) {
			if (!targetRuleSet.getConstraints().containsKey(constraint.getId())) {
				targetRuleSet.getConstraints().put(constraint.getId(), constraint);
				resolveConcepts(constraint.getRequiredConcepts(), targetRuleSet);
			}
		}
	}

	/**
	 * Resolve the given selected concept names into the target rules set.
	 * 
	 * @param concepts
	 *            The selected concept names.
	 * @param targetRuleSet
	 *            The target rules set.
	 */
	private void resolveConcepts(Collection<Concept> concepts, RuleSet targetRuleSet) {
		for (Concept concept : concepts) {
			if (!targetRuleSet.getConcepts().containsKey(concept.getId())) {
				targetRuleSet.getConcepts().put(concept.getId(), concept);
				resolveConcepts(concept.getRequiredConcepts(), targetRuleSet);
			}
		}
	}

	/**
	 * Return the selected concepts.
	 * 
	 * @param conceptNames
	 *            The list of concept names.
	 * @param ruleSet
	 *            The {@link RuleSet}.
	 * @return The selected concepts.
	 * @throws RuleSetResolverException
	 *             If an undefined concept is referenced.
	 */
	private List<Concept> getSelectedConcepts(List<String> conceptNames, RuleSet ruleSet) throws RuleSetResolverException {
		final List<Concept> selectedConcepts = new ArrayList<>();
		if (conceptNames != null) {
			for (String conceptName : conceptNames) {
				Concept concept = ruleSet.getConcepts().get(conceptName);
				if (concept == null) {
					throw new RuleSetResolverException("The concept '" + conceptName + "' is not defined.");
				}
				selectedConcepts.add(concept);
			}
		}
		return selectedConcepts;
	}

	/**
	 * Return the selected constraints.
	 * 
	 * @param constraintNames
	 *            The list of constraint names.
	 * @param ruleSet
	 *            The {@link RuleSet}.
	 * @return The selected constraints.
	 * @throws RuleSetResolverException
	 *             If an undefined constraint is referenced.
	 */
	private List<Constraint> getSelectedConstraints(List<String> constraintNames, RuleSet ruleSet) throws RuleSetResolverException {
		final List<Constraint> selectedConstraints = new ArrayList<>();
		if (constraintNames != null) {
			for (String constraintName : constraintNames) {
				Constraint concept = ruleSet.getConstraints().get(constraintName);
				if (concept == null) {
					throw new RuleSetResolverException("The constraint '" + constraintName + "' is not defined.");
				}
				selectedConstraints.add(concept);
			}
		}
		return selectedConstraints;
	}

	/**
	 * Return the selected groups.
	 * 
	 * @param groupNames
	 *            The list of constraint names.
	 * @param ruleSet
	 *            The {@link RuleSet}.
	 * @return The selected groups.
	 * @throws RuleSetResolverException
	 *             If an undefined group is referenced.
	 */
	private List<Group> getSelectedGroups(List<String> groupNames, RuleSet ruleSet) throws RuleSetResolverException {
		final List<Group> selectedGroups = new ArrayList<>();
		for (String groupName : groupNames) {
			Group group = ruleSet.getGroups().get(groupName);
			if (group != null) {
				selectedGroups.add(group);
			} else {
				LOGGER.warn("Group '{}' is not defined, skipping.", groupName);
			}
		}
		return selectedGroups;
	}

}
