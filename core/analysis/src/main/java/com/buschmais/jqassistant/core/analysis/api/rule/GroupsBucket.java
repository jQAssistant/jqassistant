package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Collection;

public class GroupsBucket extends AbstractRuleBucket<Group, NoGroupException, DuplicateGroupException> {
    @Override
    protected String getRuleTypeName() {
        return "group";
    }

    @Override
    protected DuplicateGroupException newDuplicateRuleException(String message) {
        return new DuplicateGroupException(message);
    }

    @Override
    protected NoGroupException newNoRuleException(String message) {
        return new NoGroupException(message);
    }

    public Collection<String> getGroupIds() {
        return getRuleIds();
    }

    public void addGroups(GroupsBucket bucket) throws DuplicateGroupException {
        addAll(bucket);
    }

    public Group getGroup(String id) throws NoGroupException {
        return get(id);
    }

    public Collection<Group> getGroups() {
        return getAll();
    }
}
