package com.buschmais.jqassistant.core.report;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.ReportPlugin.Default;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.impl.CompositeReportPlugin;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Group;
import com.buschmais.jqassistant.core.rule.api.model.Report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class CompositeReportPluginTest {

    @Mock
    ReportPlugin reportPlugin1;

    @Mock
    ReportPlugin reportPlugin2;

    @Mock
    ReportPlugin reportPlugin3;

    @Mock
    ReportPlugin selectableReportPlugin1;

    @Mock
    ReportPlugin selectableReportPlugin2;

    @Mock
    private Group group;

    @Mock
    private Result<?> conceptResult;

    @Mock
    private Result<?> constraintResult;

    private CompositeReportPlugin compositeReportPlugin;

    @BeforeEach
    public void setUp() {
        Map<String, ReportPlugin> reportPlugins = new HashMap<>();
        reportPlugins.put("plugin1", new DefaultReportPlugin(reportPlugin1));
        reportPlugins.put("plugin2", new DefaultReportPlugin(reportPlugin2));
        reportPlugins.put("plugin3", new DefaultReportPlugin(reportPlugin3));
        reportPlugins.put("selectablePlugin1", selectableReportPlugin1);
        reportPlugins.put("selectablePlugin2", selectableReportPlugin2);
        compositeReportPlugin = new CompositeReportPlugin(reportPlugins, new HashSet<>(asList("plugin1", "plugin2")));
    }

    @Test
    public void noSelection() throws ReportException {
        Concept concept = getRule(Concept.class);
        Constraint constraint = getRule(Constraint.class);

        write(concept, constraint);

        verifyInvoked(concept, reportPlugin1, reportPlugin2);
        verifyNotInvoked(concept, reportPlugin3, selectableReportPlugin1, selectableReportPlugin2);
        verifyInvoked(constraint, reportPlugin1, reportPlugin2);
        verifyNotInvoked(constraint, reportPlugin3, selectableReportPlugin1, selectableReportPlugin2);
        verifyGroup();
    }

    @Test
    public void selectOneWriter() throws ReportException {
        Concept concept = getRule(Concept.class, "selectablePlugin1");
        Constraint constraint = getRule(Constraint.class, "selectablePlugin1");

        write(concept, constraint);

        verifyInvoked(concept, reportPlugin1, reportPlugin2, selectableReportPlugin1);
        verifyNotInvoked(concept, reportPlugin3, selectableReportPlugin2);
        verifyInvoked(constraint, reportPlugin1, reportPlugin2, selectableReportPlugin1);
        verifyNotInvoked(constraint, reportPlugin3, selectableReportPlugin2);
        verifyGroup();
    }

    @Test
    public void selectMultiplePlugins() throws ReportException {
        Concept concept = getRule(Concept.class, "selectablePlugin1", "selectablePlugin2");
        Constraint constraint = getRule(Constraint.class, "selectablePlugin1", "selectablePlugin2");

        write(concept, constraint);

        verifyInvoked(concept, reportPlugin1, reportPlugin2, selectableReportPlugin1, selectableReportPlugin2);
        verifyInvoked(constraint, reportPlugin1, reportPlugin2, selectableReportPlugin1, selectableReportPlugin2);
        verifyNotInvoked(concept, reportPlugin3);
        verifyNotInvoked(constraint, reportPlugin3);
        verifyGroup();
    }

    private void verifyGroup() throws ReportException {
        for (ReportPlugin reportPlugin : asList(reportPlugin1, reportPlugin2, selectableReportPlugin1, selectableReportPlugin2)) {
            verify(reportPlugin).begin();
            verify(reportPlugin).end();
        }
        for (ReportPlugin reportPlugin : asList(reportPlugin1, reportPlugin2)) {
            verify(reportPlugin).beginGroup(group);
            verify(reportPlugin).endGroup();
        }
    }

    private void verifyInvoked(Concept concept, ReportPlugin... reportPlugins) throws ReportException {
        for (ReportPlugin reportPlugin : reportPlugins) {
            verify(reportPlugin).beginConcept(concept);
            verify(reportPlugin).setResult(conceptResult);
            verify(reportPlugin).endConcept();
        }
    }

    private void verifyNotInvoked(Concept concept, ReportPlugin... reportPlugins) throws ReportException {
        for (ReportPlugin reportPlugin : reportPlugins) {
            verify(reportPlugin, never()).beginConcept(concept);
            verify(reportPlugin, never()).setResult(conceptResult);
            verify(reportPlugin, never()).endConcept();
        }
    }

    private void verifyInvoked(Constraint constraint, ReportPlugin... reportPlugins) throws ReportException {
        for (ReportPlugin reportPlugin : reportPlugins) {
            verify(reportPlugin).beginConstraint(constraint);
            verify(reportPlugin).setResult(constraintResult);
            verify(reportPlugin).endConstraint();
        }
    }

    private void verifyNotInvoked(Constraint constraint, ReportPlugin... reportPlugins) throws ReportException {
        for (ReportPlugin reportPlugin : reportPlugins) {
            verify(reportPlugin, never()).beginConstraint(constraint);
            verify(reportPlugin, never()).setResult(constraintResult);
            verify(reportPlugin, never()).endConstraint();
        }
    }

    private void write(Concept concept, Constraint constraint) throws ReportException {
        compositeReportPlugin.begin();

        compositeReportPlugin.beginGroup(group);

        compositeReportPlugin.beginConcept(concept);
        compositeReportPlugin.setResult(conceptResult);
        compositeReportPlugin.endConcept();

        compositeReportPlugin.beginConstraint(constraint);
        compositeReportPlugin.setResult(constraintResult);
        compositeReportPlugin.endConstraint();

        compositeReportPlugin.endGroup();

        compositeReportPlugin.end();
    }

    private <T extends ExecutableRule> T getRule(Class<T> type, String... reportTypes) {
        T rule = mock(type);
        Report report = mock(Report.class);
        if (reportTypes.length > 0) {
            Set<String> selection = new HashSet<>(asList(reportTypes));
            when(report.getSelectedTypes()).thenReturn(selection);
        } else {
            when(report.getSelectedTypes()).thenReturn(null);
        }

        when(rule.getReport()).thenReturn(report);
        return rule;
    }

    @Default
    private static final class DefaultReportPlugin implements ReportPlugin {

        private final ReportPlugin delegate;

        private DefaultReportPlugin(ReportPlugin delegate) {
            this.delegate = delegate;
        }

        @Override
        public void initialize() throws ReportException {
            delegate.initialize();
        }

        @Override
        public void configure(ReportContext reportContext, Map<String, Object> properties) throws ReportException {
            delegate.configure(reportContext, properties);
        }

        @Override
        public void begin() throws ReportException {
            delegate.begin();
        }

        @Override
        public void end() throws ReportException {
            delegate.end();
        }

        @Override
        public void beginConcept(Concept concept) throws ReportException {
            delegate.beginConcept(concept);
        }

        @Override
        public void endConcept() throws ReportException {
            delegate.endConcept();
        }

        @Override
        public void beginGroup(Group group) throws ReportException {
            delegate.beginGroup(group);
        }

        @Override
        public void endGroup() throws ReportException {
            delegate.endGroup();
        }

        @Override
        public void beginConstraint(Constraint constraint) throws ReportException {
            delegate.beginConstraint(constraint);
        }

        @Override
        public void endConstraint() throws ReportException {
            delegate.endConstraint();
        }

        @Override
        public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
            delegate.setResult(result);
        }
    }
}
