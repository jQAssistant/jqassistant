package com.buschmais.jqassistant.core.analysis.api;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.impl.AsciiDocRuleSetReader;
import com.buschmais.jqassistant.core.analysis.impl.XmlRuleSetReader;

/**
 * @author mh
 * @since 12.10.14
 */
public class CompoundRuleSetReader implements RuleSetReader {
    @Override
    public RuleSet read(List<? extends RuleSource> sources) {
        RuleSetReader xmlReader = new XmlRuleSetReader();
        RuleSet xmlRuleSet = xmlReader.read(sources);
        RuleSetReader adocReader = new AsciiDocRuleSetReader();
        RuleSet adocRuleSet = adocReader.read(sources);
        return new RuleSet(mergeMaps(xmlRuleSet.getConcepts(), adocRuleSet.getConcepts()),
                mergeMaps(xmlRuleSet.getConstraints(), adocRuleSet.getConstraints()), mergeMaps(xmlRuleSet.getGroups(), adocRuleSet.getGroups()), mergeMaps(
                        xmlRuleSet.getMetricGroups(), adocRuleSet.getMetricGroups()));
    }

    public Group createGroup(RuleSet xmlRuleSet, RuleSet adocRuleSet) {
        Group group = new Group();
        group.setConcepts(mergeCollections(xmlRuleSet.getConcepts().values(), adocRuleSet.getConcepts().values()));
        group.setConstraints(mergeCollections(xmlRuleSet.getConstraints().values(), adocRuleSet.getConstraints().values()));
        group.setGroups(new LinkedHashSet<>(mergeMaps(xmlRuleSet.getGroups(), adocRuleSet.getGroups()).values()));
        return group;
    }

    private <T> Set<T> mergeCollections(Collection<T> coll1, Collection<T> coll2) {
        LinkedHashSet<T> result = new LinkedHashSet<>(coll1);
        result.addAll(coll2);
        return result;
    }

    public <T> Map<String, T> mergeMaps(Map<String, T> map1, Map<String, T> map2) {
        Map<String, T> result = new LinkedHashMap<>();
        if (map1 != null && !map1.isEmpty())
            result.putAll(map1);
        if (map2 != null && !map2.isEmpty())
            result.putAll(map2);
        return result;
    }
}
