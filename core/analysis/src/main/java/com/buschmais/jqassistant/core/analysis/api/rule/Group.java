package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines a group.
 */
public class Group implements Rule {

	private String id;

	private Set<Concept> concepts = new HashSet<>();

	private Set<Constraint> constraints = new HashSet<Constraint>();

	private Set<Group> groups = new HashSet<Group>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<Concept> getConcepts() {
		return concepts;
	}

	public void setConcepts(Set<Concept> concepts) {
		this.concepts = concepts;
	}

	public Set<Constraint> getConstraints() {
		return constraints;
	}

	public void setConstraints(Set<Constraint> constraints) {
		this.constraints = constraints;
	}

	public Set<Group> getGroups() {
		return groups;
	}

	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Group that = (Group) o;
		if (!id.equals(that.id))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return "Constraint Group " + id;
	}

}
