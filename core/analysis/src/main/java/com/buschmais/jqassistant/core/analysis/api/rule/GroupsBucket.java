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
}
