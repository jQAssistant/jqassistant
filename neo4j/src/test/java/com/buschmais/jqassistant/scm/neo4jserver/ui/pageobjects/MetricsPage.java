package com.buschmais.jqassistant.scm.neo4jserver.ui.pageobjects;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
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
    // We can't use this here: @FindBy(id = "metricGroupIdSelect")
    private Select metricGroupSelectBox;

    @FindBy(id ="metricId")
    private WebElement metricIdElement;

    public MetricsPage(WebDriver driver) {
        this.driver = driver;

        metricGroupSelectBox = new Select(driver.findElement(By.id("metricGroupIdSelect")));
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
        metricGroupSelectBox.selectByValue(metricGroupId);

        // the selection triggers an AJAX request, we want to wait until this request is finished: all components rely on the result of this request
        // the request is successfully finished if the <div/> with the id="metricId" has some none empty text

        new WebDriverWait(driver, 10 /*seconds*/)
                .pollingEvery(1, TimeUnit.SECONDS)
                .until(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(WebDriver webDriver) {
                        String metricId = webDriver.findElement(By.id("metricId")).getText();
                        return !metricId.isEmpty();
                    }
                });
    }

    /**
     * If a metric group is selected, a metric ID will be selected by default. This
     * function returns the currently active metric.
     *
     * @return {@code null} if no metric group is selected; a metric ID otherwise
     */
    public String getCurrentMetricId() {

        if (metricGroupSelectBox.getFirstSelectedOption() == null) {
            // nothing is selected in the metric group select box, so no metric ID could be set
            return null;
        }
        return metricIdElement.getText();
    }
}
