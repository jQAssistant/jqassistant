package com.buschmais.jqassistant.scm.maven.shell;

import com.buschmais.jqassistant.core.analysis.api.*;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.analysis.impl.RuleSelectorImpl;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.common.AnalysisHelper;
import org.apache.maven.plugin.MojoExecutionException;
import org.neo4j.helpers.Service;
import org.neo4j.shell.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service.Implementation(App.class)
public class AnalyzeApp extends AbstractJQAssistantApp {

	private static final Pattern CONCEPTS_PATTERN = Pattern.compile("concepts=(.*)");
	private static final Pattern CONSTRAINTS_PATTERN = Pattern.compile("constraints=(.*)");
	private static final Pattern GROUPS_PATTERN = Pattern.compile("groups=(.*)");

	private RuleSet availableRules;

	public AnalyzeApp() throws PluginReaderException {
		super();
		availableRules = readRuleSet();
	}

	@Override
	public String getCommand() {
		return "analyze";
	}

	@Override
	public Continuation execute(AppCommandParser parser, Session session, final Output out) throws Exception {
		List<String> conceptNames = new ArrayList<>();
		List<String> constraintNames = new ArrayList<>();
		List<String> groupNames = new ArrayList<>();
		for (String argument : parser.arguments()) {
			if (parseArgument(CONCEPTS_PATTERN, argument, conceptNames)) {
			} else if (parseArgument(CONSTRAINTS_PATTERN, argument, constraintNames)) {
			} else if (parseArgument(GROUPS_PATTERN, argument, groupNames)) {
			} else {
				throw new IllegalArgumentException("Illegal argument " + argument);
			}
		}
		AnalysisHelper analysisHelper = new AnalysisHelper(new ShellConsole(out));
		RuleSelector ruleSelector = new RuleSelectorImpl();
		RuleSet effectiveRuleSet = ruleSelector.getEffectiveRuleSet(availableRules, conceptNames, constraintNames, groupNames);
		InMemoryReportWriter reportWriter = new InMemoryReportWriter();
		Store store = getStore();
		store.start(Collections.<Class<?>> emptyList());
		Analyzer analyzer = new AnalyzerImpl(store, reportWriter);
		analyzer.execute(effectiveRuleSet);
		analysisHelper.verifyConceptResults(reportWriter);
		analysisHelper.verifyConstraintViolations(reportWriter);
		store.stop();
		return Continuation.INPUT_COMPLETE;
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
