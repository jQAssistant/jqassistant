package com.buschmais.jqassistant.mojo.shell;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.impl.RuleSetReaderImpl;
import com.buschmais.jqassistant.core.pluginmanager.api.RulePluginRepository;
import com.buschmais.jqassistant.core.pluginmanager.impl.RulePluginRepositoryImpl;
import org.neo4j.shell.Output;
import org.neo4j.shell.impl.AbstractApp;

import javax.xml.transform.Source;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by dimahler on 3/14/14.
 */
public abstract class AbstractJQAssistantApp extends AbstractApp {

    public static final String LOG_LINE_PREFIX = "  \"";
    /**
     * The rules reader instance.
     */
    private RuleSetReader ruleSetReader;
    private RulePluginRepository rulePluginRepository;

    AbstractJQAssistantApp() throws PluginReaderException {
        rulePluginRepository = new RulePluginRepositoryImpl();
        ruleSetReader = new RuleSetReaderImpl();

    }

    @Override
    public final String getName() {
        return "jqa:" + getCommand();
    }

    protected abstract String getCommand();

    protected RuleSet readRuleSet() {
        List<Source> ruleSources = rulePluginRepository.getRuleSources();
        return ruleSetReader.read(ruleSources);
    }

    /**
     * Logs the given {@link RuleSet} on level info.
     *
     * @param ruleSet The {@link RuleSet}.
     */
    protected void printRuleSet(RuleSet ruleSet, Output output) throws RemoteException {
        output.println("Groups [" + ruleSet.getGroups().size() + "]");
        for (Group group : ruleSet.getGroups().values()) {
            output.println(LOG_LINE_PREFIX + group.getId() + "\"");
        }
        output.println("Constraints [" + ruleSet.getConstraints().size() + "]");
        for (Constraint constraint : ruleSet.getConstraints().values()) {
            output.println(LOG_LINE_PREFIX + constraint.getId() + "\" - " + constraint.getDescription());
        }
        output.println("Concepts [" + ruleSet.getConcepts().size() + "]");
        for (Concept concept : ruleSet.getConcepts().values()) {
            output.println(LOG_LINE_PREFIX + concept.getId() + "\" - " + concept.getDescription());
        }
        if (!ruleSet.getMissingConcepts().isEmpty()) {
            output.println("Missing concepts [" + ruleSet.getMissingConcepts().size() + "]");
            for (String missingConcept : ruleSet.getMissingConcepts()) {
                output.println(LOG_LINE_PREFIX + missingConcept);
            }
        }
        if (!ruleSet.getMissingConstraints().isEmpty()) {
            output.println("Missing constraints [" + ruleSet.getMissingConstraints().size() + "]");
            for (String missingConstraint : ruleSet.getMissingConstraints()) {
                output.println(LOG_LINE_PREFIX + missingConstraint);
            }
        }
        if (!ruleSet.getMissingGroups().isEmpty()) {
            output.println("Missing groups [" + ruleSet.getMissingGroups().size() + "]");
            for (String missingGroup : ruleSet.getMissingGroups()) {
                output.println(LOG_LINE_PREFIX + missingGroup);
            }
        }
    }

}
