'use strict';

/**
 *  Object to keep references to the metric groups.
 */
var MetricGroups = {
    groups: [],
    activeMetric: null,
    getGroupByGroupId: function (groupId) {
        var group = null;
        $.each(this.groups, function(index, value){
            if (value["id"] == groupId) {
                group = value;
            }
        });
        return group;
    },
    getMetricByGroupIdAndMetricId: function (groupId, metricId) {
        var group = this.getGroupByGroupId(groupId);
        if (!group) {
            return null;
        }
        if (!metricId) {
            return group["metrics"][0];
        } else {
            var metrics = group["metrics"];
            var metric = null;
            $.each(metrics, function(index, value) {
                if (value["id"] == metricId) {
                    metric = value;
                }
            });
            return metric;
        }
    },
    getGroupIds: function () {
        var groupIds = [];
        $.each(this.groups, function(index, value){
            groupIds.push(value["id"]);
        });
        return groupIds;
    },
    /* Initialize the groups. Set the previous/next metrics and the "parent" (group) of each metric. */
    initializeGroups: function (newGroups) {
        $.each(newGroups, function(index, theGroup) {
            if (theGroup["metrics"] == null) {
                return; // continue;
            }
            var metrics = theGroup["metrics"];
            $.each(metrics, function (index2, metric) {
                // set the group value (reference to the parent)
                metric["group"] = theGroup;
                // set the previous value
                if (index2 > 0) {
                    metric["previous"] = metrics[index2 - 1];
                } else {
                    metric["previous"] = null;
                }
                // set the next value
                if ((index2 + 1) < metrics.length) {
                    metric["next"] = metrics[index2 + 1];
                } else {
                    metric["next"] = null;
                }
            });
        });
        this.groups = newGroups;
    },
    toString: function () {
        var aString = "MetricGroups {";
        var groupsLength = this.groups.length;
        $.each(this.groups, function(index, value) {
            aString += value["id"];
            if ((index + 1) < groupsLength) {
                aString += ", ";
            }
        });
        aString += "}";
        return aString;
    }
};

/**
 *  Load the metric groups from the server.
 */
function loadMetricGroups() {

    console.log("loadMetricGroups()");

    var url = JqaConstants.REST_METRIC_GROUPS_URL;

    console.log("Loading metrics from " + url);

    removeAlert();
    showSpinner();

    $.getJSON(url)
        .done(function (json) { /* the success function */
            console.log("Successfully loaded metrics.");

            if (!json) {
                // there was an error in the call (or at server side)
                showAlert("No result received from the server. Please check the logs.");
            }

            updateMetricGroups(json);

            runMetricFromUrlParameters();
        })
        .fail(function (jqxhr, textStatus, error) { /* the failed function */

            var errorMessage;
            if (jqxhr.status == 0) {
                errorMessage = "Error loading metrics: Is the service down?";
            } else {
                errorMessage = "Error loading metrics: " + jqxhr.status + " " + jqxhr.statusText;
            }
            console.log(errorMessage);

            showAlert(errorMessage);
        })
        .always(function () { /* always executed after the AJAX call */
            removeSpinner();
            console.log("Finished loading metrics.");
        });
}

/**
 *  Called if the metric loading succeeded.
 *
 *  @param metricsGroups the groups that have been loaded
 */
function updateMetricGroups(metricsGroups) {

    console.log("updateMetricGroups(...)");

    if (!metricsGroups || metricsGroups.length == 0) {
        console.log("Error - metrics loaded are empty: " + metricsGroups);
        return;
    }

    MetricGroups.initializeGroups(metricsGroups);

    console.log("Got: " + MetricGroups.toString());

    var groupIds = MetricGroups.getGroupIds();
    var select = $("#metricGroupIdSelect");
    for (var i = 0; i < groupIds.length; i++) {
        select.append("<option value='" + groupIds[i] + "'>" + groupIds[i] + "</option>")
    }

    select[0].selectedIndex = -1;
    $(".metricGroup-details").hide();
    $(".metricGroup-details-button").hide();
    $(".metric-details").hide();
}

/**
 * Run the metric from the URL parameters.
 */
function runMetricFromUrlParameters() {

    console.log("runMetricFromUrlParameters()");

    var urlGroupId = getUrlHashParameterValue(JqaConstants.GROUP_ID);
    var urlMetricId = getUrlHashParameterValue(JqaConstants.METRICS_ID);

    if (!urlGroupId || !urlMetricId) {
        console.log("There are no URL parameter to run a metric for.");
        return;
    }

    $("#metricGroupIdSelect").val(urlGroupId);

    var group = MetricGroups.getGroupByGroupId(urlGroupId);
    var metric = MetricGroups.getMetricByGroupIdAndMetricId(urlGroupId, urlMetricId);

    if (!group || !metric) {
        console.log("No metric group or metric found for '" + urlGroupId + "' and '" + urlMetricId);
        return;
    }

    MetricGroups.activeMetric = metric;

    runMetric(getUrlHashParameters());
}

/**
 * Update the UI components.
 *
 * @param group the metric group
 * @param metric the metric
 */
function updateUiComponents(group, metric) {

    $("#metricGroupDescription").text(group["description"]);
    $(".metricGroup-details").show();

    $(".metricGroup-details-button").show();

    $("#metricId").text(metric["id"]);
    $("#metricQuery").text(metric["query"]);
    $("#metricDescription").text(metric["description"]);

    $("#showMetricsPanel").show();

    updateBreadcrumb(metric);

    updateUpButton(metric);
}

/**
 * Update the breadcrumb for the given metric.
 *
 * @param metric the metric to use
 */
function updateBreadcrumb(metric) {

    var breadcrumb = [];

    breadcrumb.push(metric);
    var currentMetric = metric["previous"];
    while (currentMetric) {
        breadcrumb.push(currentMetric);
        currentMetric = currentMetric["previous"];
    }
    breadcrumb.reverse();

    var breadcrumbOl = $("#metricBreadcrumb");
    breadcrumbOl.empty();

    var breadcrumbLength = breadcrumb.length;
    $.each(breadcrumb, function (index, value) {

        // get the parameters necessary to run the metric (again)
        // at this state they are all available in the URL
        var parameterMap = {};
        parameterMap[JqaConstants.GROUP_ID] = value["group"]["id"];
        parameterMap[JqaConstants.METRICS_ID] = value["id"];
        $.each(value["parameters"], function(index2, value2){
            if (value2) {
                parameterMap[value2] = getUrlHashParameterValue(value2);
            }
        });

        // add the active class to the last item in the breadcrumb
        var activeClass = ((index + 1) >= breadcrumbLength) ? " active" : "";

        // create a new li element
        var li = $("<li></li>", {
            "class": activeClass
        });

        if (activeClass === "") {
            // create a new <a/> element
            var a = $("<a></a>", {
                "href": "#",
                "title": value["description"],
                "click": function (event) {
                    event.preventDefault();
                    runMetric(parameterMap);
                }
            });

            // set the text (the id) to the <a/>
            a.text(value["id"]);
            // append the <a/> to the <li/>
            li.append(a);

        } else {
            // just set the value of the ID to the <li/>
            li.text(value["id"]);
        }

        breadcrumbOl.append(li);
    });
}

/**
 * Set the visibility of the up button, and changed its click event.
 *
 * @param metric the metric used to update the content of the button
 */
function updateUpButton(metric) {

    var previousMetric = metric["previous"];

    var upButtonDiv = $("#metricUp");

    if (!previousMetric) {
        upButtonDiv.hide();
        return;
    } else {
        upButtonDiv.show();
    }

    var parameterMap = {};
    parameterMap[JqaConstants.GROUP_ID] = previousMetric["group"]["id"];
    parameterMap[JqaConstants.METRICS_ID] = previousMetric["id"];
    $.each(previousMetric["parameters"], function(index, value){
        if (value) {
            parameterMap[value] = getUrlHashParameterValue(value);
        }
    });

    upButtonDiv.find("a").off("click");
    upButtonDiv.find("a").on("click", (function(event) {
        event.preventDefault();
        runMetric(parameterMap);
    }));
}

/**
 *  Select a specific metric group.
 *
 *  @param metricGroupId the ID of the group
 *  @param metricId the ID of the metric - can be null; if null, the first metric of the group is selected
 */
function selectMetric(metricGroupId, metricId) {

    console.log("selectMetric(" + metricGroupId + ", " + metricId + ")");

    var group = MetricGroups.getGroupByGroupId(metricGroupId);
    if (!group) {
        console.log("No group found for '" + metricGroupId + "'.");
        return;
    }

    var metric = MetricGroups.getMetricByGroupIdAndMetricId(metricGroupId, metricId);
    if (!metric) {
        console.log("No metric found for '" + metricId + "'.");
        return;
    }

    MetricGroups.activeMetric = metric;

    // now after setting all display values, run the metric at the server
    var map = {};
    map[JqaConstants.GROUP_ID] = metricGroupId;
    map[JqaConstants.METRICS_ID] = metric["id"];
    runMetric(map)
}

/**
 *  Toggle the visibility of the details elements.
 *
 *  @param button the button the fired the event
 */
function toggleMetricDetails(button) {

    console.log("toggleMetricDetails()");

    var chevron = $(button).data("chevron");
    if (chevron === null) {
        chevron = "up";
    }

    if (chevron === "up") {
        $(button).find(".glyphicon").removeClass("glyphicon-chevron-up");
        $(button).find(".glyphicon").addClass("glyphicon-chevron-down");
        chevron = "down";
    } else {
        $(button).find(".glyphicon").removeClass("glyphicon-chevron-down");
        $(button).find(".glyphicon").addClass("glyphicon-chevron-up");
        chevron = "up"
    }
    $(button).data("chevron", chevron);

    $(".metric-details").toggle();
}

/**
 *  Run the given metric.
 *
 *  @param parameterMap an map of additional parameters
 */
function runMetric(parameterMap) {

    console.log("runMetric(" + parameterMap[JqaConstants.GROUP_ID] + ", " + parameterMap[JqaConstants.METRICS_ID] + ")");

    $("#treemapContainer").empty();

    var metric = MetricGroups.getMetricByGroupIdAndMetricId(parameterMap[JqaConstants.GROUP_ID], parameterMap[JqaConstants.METRICS_ID]);
    if (!metric) {
        console.log("No metric found for '" + parameterMap[JqaConstants.METRICS_ID] + "'. The metric can't be run. Don't do anything.");
        return;
    }

    var url = JqaConstants.REST_RUN_METRIC_URL;

    removeAlert();
    showSpinner();

    $.getJSON(url, parameterMap)
        .done(function (json) { /* the success function */
            console.log("Successfully ran metric.");

            if (!json) {
                // there was an error in the call
                showAlert("No result received from the server. Please check the logs.");
                return;
            }

            if (json["error"]) {
                showAlert(json["error"]);
                return;
            }

            if (!json["result"]) {
                // there was an error in the call
                showAlert("No result received from the server. Please check the logs.");
                return;
            }

            // only set the hash parameter if running the metric succeeded
            setUrlHashParameter(parameterMap);

            // set the successfully ran metric as the active metric
            MetricGroups.activeMetric = metric;

            updateUiComponents(metric["group"], metric);

            // render the chart
            jqad3.renderChart(json["result"]);
        })
        .fail(function (jqxhr, textStatus, error) { /* the failed function */
            var errorMessage = "Error running metric: " + jqxhr.status + " " + jqxhr.statusText;
            console.log(errorMessage);

            showAlert(errorMessage);
        })
        .always(function () { /* always executed after the AJAX call */
            removeSpinner();
            console.log("Finished running metric.");
        });
}