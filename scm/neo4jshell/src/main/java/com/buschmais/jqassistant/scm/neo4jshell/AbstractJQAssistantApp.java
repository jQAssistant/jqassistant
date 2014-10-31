package com.buschmais.jqassistant.scm.neo4jshell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.shell.AppCommandParser;
import org.neo4j.shell.AppShellServer;
import org.neo4j.shell.impl.AbstractApp;
import org.neo4j.shell.kernel.GraphDatabaseShellServer;

import com.buschmais.jqassistant.core.analysis.api.RuleSelector;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.RuleSetResolverException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.impl.RuleSelectorImpl;
import com.buschmais.jqassistant.core.analysis.impl.XmlRuleSetReader;
import com.buschmais.jqassistant.core.plugin.api.ModelPluginRepository;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.impl.ModelPluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.RulePluginRepositoryImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.GraphDbStore;

/**
 * Abstract base implementation for shell commands.
 */
public abstract class AbstractJQAssistantApp extends AbstractApp {

    private static final Pattern CONCEPTS_PATTERN = Pattern.compile("concepts=(.*)");
    private static final Pattern CONSTRAINTS_PATTERN = Pattern.compile("constraints=(.*)");
    private static final Pattern GROUPS_PATTERN = Pattern.compile("groups=(.*)");

    private PluginConfigurationReader pluginConfigurationReader;
    private RulePluginRepository rulePluginRepository;
    /**
     * The rules reader instance.
     */
    private RuleSetReader ruleSetReader;

    private Store store = null;

    protected AbstractJQAssistantApp() throws PluginRepositoryException {
        pluginConfigurationReader = new PluginConfigurationReaderImpl();
        rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
        ruleSetReader = new XmlRuleSetReader();
    }

    @Override
    public final String getName() {
        return "jqa:" + getCommand();
    }

    protected abstract String getCommand();

    protected RuleSet getAvailableRules() {
        List<RuleSource> ruleSources = rulePluginRepository.getRuleSources();
        return ruleSetReader.read(ruleSources);
    }

    protected Store getStore() {
        if (store == null) {
            AppShellServer server = getServer();
            if (!(server instanceof GraphDatabaseShellServer)) {
                throw new IllegalStateException("Unexpected server type " + server);
            }
            GraphDatabaseService db = ((GraphDatabaseShellServer) server).getDb();
            store = new GraphDbStore(db);
        }
        return store;
    }

    protected ModelPluginRepository getModelPluginRepository() {
        try {
            return new ModelPluginRepositoryImpl(pluginConfigurationReader);
        } catch (PluginRepositoryException e) {
            throw new IllegalStateException("Cannot get model plugin repository", e);
        }
    }

    protected RuleSet getEffectiveRuleSet(AppCommandParser parser) throws RuleSetResolverException {
        List<String> conceptNames = new ArrayList<>();
        List<String> constraintNames = new ArrayList<>();
        List<String> groupNames = new ArrayList<>();
        for (String argument : parser.arguments()) {
            if (parseArgument(CONCEPTS_PATTERN, argument, conceptNames))
                ;
            else if (parseArgument(CONSTRAINTS_PATTERN, argument, constraintNames))
                ;
            else if (parseArgument(GROUPS_PATTERN, argument, groupNames))
                ;
            else {
                throw new IllegalArgumentException("Illegal argument " + argument);
            }
        }
        RuleSet availableRules = getAvailableRules();
        RuleSelector ruleSelector = new RuleSelectorImpl();
        return ruleSelector.getEffectiveRuleSet(availableRules, conceptNames, constraintNames, groupNames);
    }

    private boolean parseArgument(Pattern pattern, String argument, List<String> values) {
        Matcher matcher = pattern.matcher(argument);
        if (matcher.matches()) {
            values.addAll(Arrays.asList(matcher.group(1).split(",")));
            return true;
        }
        return false;
    }
}
