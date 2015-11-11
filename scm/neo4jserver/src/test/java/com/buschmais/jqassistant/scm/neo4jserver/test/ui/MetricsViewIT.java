package com.buschmais.jqassistant.scm.neo4jserver.test.ui;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.rule.NoMetricGroupException;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.PageFactory;

import com.buschmais.jqassistant.core.analysis.api.rule.Metric;
import com.buschmais.jqassistant.core.analysis.api.rule.MetricGroup;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.neo4jserver.test.ui.pageobjects.MetricsPage;

/**
 * Test the metrics html page.
 */
public class MetricsViewIT extends AbstractUITest {

    /** This metric group is currently delivered by default. */
    private static final String METRIC_GROUP_ID_artifactDependencies = "metric:ArtifactDependencies";

    /** The metrics page. */
    private MetricsPage metricsPage;

    @Override
    protected String getWebPage() {
        return "metrics.html";
    }

    @Before
    public void setup() {

        metricsPage = PageFactory.initElements(driver, MetricsPage.class);
    }

    /**
     * This test proves if the metrics service successfully returns the metric
     * groups.
     */
    @Test
    public void testGetMetricIds() {

        Set<String> ruleSetMetricGroupIds = ruleSet.getMetricGroupsBucket().getIds();
        assertFalse(ruleSetMetricGroupIds.isEmpty());

        Set<String> metricGroupIds = metricsPage.getMetricGroupIds();

        assertFalse(metricGroupIds.isEmpty());

        // check if the metric group ids of the rule set are available in the
        // metrics page
        Set<String> missingGroupIds = new HashSet<>();
        for (String ruleSetMetricGroupId : ruleSetMetricGroupIds) {
            if (!metricGroupIds.contains(ruleSetMetricGroupId)) {
                missingGroupIds.add(ruleSetMetricGroupId);
            }
        }
        assertTrue("Missing groups from rule set: " + missingGroupIds, missingGroupIds.isEmpty());

        assertTrue(metricGroupIds.contains(METRIC_GROUP_ID_artifactDependencies));
    }

    /**
     * This test lets the metric service run a metric.
     */
    @Test
    public void testMetricGroupSelection() throws IOException, NoMetricGroupException {
        scanClasses("core", Store.class);
        scanClassPathDirectory("server-test", getClassesDirectory(MetricsViewIT.class));

        // this will run the first metric of the group
        metricsPage.selectMetricGroup(METRIC_GROUP_ID_artifactDependencies);

        // open the metrics details -> this is important for the next step,
        // metricsPage.getCurrentMetricId() only returns a value if the element
        // is visible
        metricsPage.openMetricDetails();

        MetricGroup metricGroup = ruleSet.getMetricGroupsBucket().getById(METRIC_GROUP_ID_artifactDependencies);
        Metric firstMetric = new ArrayList<>(metricGroup.getMetrics().values()).get(0);
        // if running the metric succeeded, the metric ID field in the metric
        // page is filled with the metric ID
        assertEquals(firstMetric.getId(), metricsPage.getCurrentMetricId());

        // the breadcrumb is updated and shows only one element: the first
        // metric id
        List<String> breadcrumb = metricsPage.getBreadcrumb();
        assertEquals("The breadcrumb must have one element.", 1, breadcrumb.size());
        assertEquals(firstMetric.getId(), breadcrumb.get(0));
    }

    /**
     * This test will click on a node in the treemap and check the result.
     */
    @Test
    public void testDrillDown() throws Exception {
        String artifactId = "server-test";
        scanClassPathDirectory(artifactId, getClassesDirectory(MetricsViewIT.class));
        scanClasses("core", Store.class);

        // TODO Applying concept 'dependency:Package' doesn't work properly, as
        // the classes are scanned directly. This is a workaround for that
        // issue.
        store.executeQuery("match " + "(a:Artifact)-[:CONTAINS]->(t:Type), " + "(p:Package)-[:CONTAINS*]->(t) " + "create (a)-[c:CONTAINS]->(p) "
                + "return count(c)");

        // for test step explanation see testMetricGroupSelection()
        metricsPage.selectMetricGroup(METRIC_GROUP_ID_artifactDependencies);
        metricsPage.openMetricDetails();

        MetricGroup metricGroup = ruleSet.getMetricGroupsBucket().getById(METRIC_GROUP_ID_artifactDependencies);
        Metric firstMetric = new ArrayList<>(metricGroup.getMetrics().values()).get(0);

        List<String> breadcrumb = metricsPage.getBreadcrumb();
        assertEquals("The breadcrumb must have one element.", 1, breadcrumb.size());
        assertEquals(firstMetric.getId(), breadcrumb.get(0));

        // this triggers the AJAX call
        metricsPage.selectNode(artifactId);

        // the breadcrumb now must contain the first and the second metric ID
        Metric secondMetric = new ArrayList<>(metricGroup.getMetrics().values()).get(1);
        breadcrumb = metricsPage.getBreadcrumb();
        assertEquals("The breadcrumb must have two elements.", 2, breadcrumb.size());
        assertEquals(firstMetric.getId(), breadcrumb.get(0));
        assertEquals(secondMetric.getId(), breadcrumb.get(1));
    }
}
