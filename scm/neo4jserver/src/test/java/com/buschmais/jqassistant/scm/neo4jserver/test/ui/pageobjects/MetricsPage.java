package com.buschmais.jqassistant.scm.neo4jserver.test.ui.pageobjects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

/**
 * Represents the metrics page.
 */
public class MetricsPage {

    /** The web driver. */
    private final WebDriver driver;

    /** The &lt;select/&gt; containing all metric group ids. */
    @FindBy(id = "metricGroupIdSelect")
    private WebElement metricGroupSelectBox;

    @FindBy(id ="metricId")
    private WebElement metricIdElement;

    @FindBy(id = "metricGroup-details-button")
    private WebElement detailsButton;

    @FindBy(id = "metricBreadcrumb")
    private WebElement breadcrumb;

    @FindBy(id = "treemapContainer")
    private WebElement treemap;

    public MetricsPage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Get the set of metric group IDs this page does currently show.
     * @return the set of IDs
     * @see com.buschmais.jqassistant.core.analysis.api.rule.MetricGroup
     */
    public Set<String> getMetricGroupIds() {

        return new WebDriverWait(driver, 5 /*seconds*/)
                .pollingEvery(1, TimeUnit.SECONDS)
                .until(new Function<WebDriver, Set<String>>() {
                    @Override
                    public Set<String> apply(WebDriver webDriver) {
                        Select select = new Select(webDriver.findElement(By.id("metricGroupIdSelect")));
                        if (select.getOptions().isEmpty()) {
                            return null;
                        }

                        Set<String> ids = new HashSet<>();
                        for (WebElement webElement : select.getOptions()) {
                            ids.add(webElement.getAttribute("value"));
                        }
                        return ids;
                    }
                });
    }

    /**
     * Use the metric group ID select box to select the metric group with the given ID.
     * @param metricGroupId the metric group ID to select
     */
    public void selectMetricGroup(String metricGroupId) {

        if (!getMetricGroupIds().contains(metricGroupId)) {
            // the desired metric group ID is none of the values in the select box
            return;
            // NOTE: getMetricGroupIds() also ensures that the AJAX call to get the metric groups was called
        }
        new Select(metricGroupSelectBox).selectByValue(metricGroupId);

        // the selection triggers an AJAX request, we want to wait until this request is finished: all components rely on the result of this request
        // the request is successfully finished if the button <div/> gets visible
        new WebDriverWait(driver, 5 /*seconds*/)
                .pollingEvery(1, TimeUnit.SECONDS)
                .until(ExpectedConditions.visibilityOf(driver.findElement(By.className("metricGroup-details-button"))));
    }

    /**
     * If a metric group is selected, a metric ID will be selected by default. This
     * function returns the currently active metric.
     *
     * @return {@code null} if no metric group is selected; a metric ID otherwise
     */
    public String getCurrentMetricId() {

        if (new Select(metricGroupSelectBox).getFirstSelectedOption() == null) {
            // nothing is selected in the metric group select box, so no metric ID could be set
            return null;
        }
        return metricIdElement.getText();
    }

    /**
     * Opens the metric details by clicking on the button.
     */
    public void openMetricDetails() {

        detailsButton.click();
    }

    /**
     * Gets what is currently visible in the breadcrumb.
     *
     * @return the breadcrumb or {@code null} if there is nothing visible
     */
    public List<String> getBreadcrumb() {

        if (!breadcrumb.isDisplayed()) {
            return null;
        }

        List<String> breadcrumbTexts = new ArrayList<>();
        for (WebElement anker : breadcrumb.findElements(By.xpath(".//li/a"))) {
            breadcrumbTexts.add(anker.getText());
        }

        breadcrumbTexts.add(breadcrumb.findElement(By.xpath(".//li[@class='active']")).getText());

        return breadcrumbTexts;
    }

    /**
     * Selects (click) a node in the treemap. The node selected must have the given text.
     * @param text the text of the node
     */
    public void selectNode(String text) {

        if (!treemap.isDisplayed()) {
            return;
        }

        WebElement node =
                treemap.findElement(By.xpath(".//div[contains(@class, 'node-d3') and text() = '" + text + "']"));

        if (node == null) {
            return;
        }

        node.click();
        // the selection triggers an AJAX request, we want to wait until this request is finished
        // the request is successfully finished if the button <div/> gets visible
        new WebDriverWait(driver, 5 /*seconds*/)
                .pollingEvery(1, TimeUnit.SECONDS)
                .until(ExpectedConditions.visibilityOf(driver.findElement(By.className("metricGroup-details-button"))));
    }
}
