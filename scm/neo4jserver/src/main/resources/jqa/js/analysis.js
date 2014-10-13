'use strict';

/**
 *  Load the metric groups from the server.
 */
function loadRuleSets() {

    console.log("loadRuleSets()");

    removeAlert();
    showSpinner();

    var url = JqaConstants.REST_ANALYSIS_RULES_URL;

    console.log("Loading rule sets from " + url);

    $.getJSON(url)
        .done(function (json) { /* the success function */
            console.log("Successfully loaded rule sets.");

            if (!json) {
                // there was an error in the call (or at server side)
                showAlert("No result received from the server. Please check the logs.");
            }

            updateUiComponents(json);
        })
        .fail(function (jqxhr, textStatus, error) { /* the failed function */

            var errorMessage;
            if (jqxhr.status == 0) {
                errorMessage = "Error loading rule sets: Is the service down?";
            } else {
                errorMessage = "Error loading rule sets: " + jqxhr.status + " " + jqxhr.statusText;
            }
            console.log(errorMessage);

            showAlert(errorMessage);
        })
        .always(function () { /* always executed after the AJAX call */
            removeSpinner();
            console.log("Finished loading rule sets.");
        });
}

/**
 * Update the UI components.
 *
 * @param ruleSets the rule sets JSON to obtain the value for updating
 */
function updateUiComponents(ruleSets) {

    console.log("updateUiComponents(...)");

    /*
     *  Concepts
     */
    var conceptsSize = ruleSets["concepts"].length;
    if (conceptsSize > 0) {
        $("#conceptsSize").text(conceptsSize);

        var listGroupConcepts = $("<div></div>");
        $.each(ruleSets["concepts"], function (index, value) {
            var listGroupItem = $("<div></div>").addClass("list-group");

            var listGroupHeading = $("<div></div>").addClass("list-group-item active");
            var button = $("<button></button>").addClass("btn btn-default")
                .append($("<span></span>").addClass("glyphicon glyphicon-play"));
            var span = $("<span></span>").text(" " + value["id"]);
            listGroupHeading.append(button);
            listGroupHeading.append(span);
            listGroupItem.append(listGroupHeading);
            listGroupItem.append($("<div></div>").addClass("list-group-item").text(value["description"]));
            listGroupItem.append($("<div></div>").addClass("list-group-item")
                .append($("<pre></pre>").text(value["cypher"])));
            var responseDiv = $("<div></div>").addClass("list-group-item");
            listGroupItem.append(responseDiv);
            listGroupConcepts.append(listGroupItem);

            button.click(function () {
                runConcept(value["id"], responseDiv)
            });
        });

        $("#concepts").append(listGroupConcepts);
    } else {
        $("#concepts").append($("<div></div>").text("There are no concepts."));
    }

    /*
     *  Constraints
     */
    var constraintsSize = ruleSets["constraints"].length;
    if (constraintsSize > 0) {
        $("#constraintsSize").text(constraintsSize);

        var listGroupConstraints = $("<div></div>");
        $.each(ruleSets["constraints"], function (index, value) {
            var listGroupItem = $("<div></div>").addClass("list-group");

            var listGroupHeading = $("<div></div>").addClass("list-group-item active");
            var button = $("<button></button>").addClass("btn btn-default")
                .append($("<span></span>").addClass("glyphicon glyphicon-play"));
            var span = $("<span></span>").text(" " + value["id"]);
            listGroupHeading.append(button);
            listGroupHeading.append(span);
            listGroupItem.append(listGroupHeading);
            listGroupItem.append($("<div></div>").addClass("list-group-item").text(value["description"]));
            listGroupItem.append($("<div></div>").addClass("list-group-item")
                .append($("<pre></pre>").text(value["cypher"])));
            var responseDiv = $("<div></div>").addClass("list-group-item");
            listGroupItem.append(responseDiv);
            listGroupConstraints.append(listGroupItem);

            button.click(function () {
                runConstraint(value["id"], responseDiv)
            });
        });

        $("#constraints").append(listGroupConstraints);
    } else {
        $("#constraints").append($("<div></div>").text("There are no constraints."));
    }

    /*
     *  Groups
     */
    var groupsSize = ruleSets["groups"].length;
    if (groupsSize > 0) {
        $("#groupsSize").text(groupsSize);

        var listGroupGroups = $("<div></div>");
        $.each(ruleSets["groups"], function (index, value) {
            var listGroupItem = $("<div></div>").addClass("list-group");

            var listGroupHeading = $("<div></div>").addClass("list-group-item active");
            var button = $("<button></button>").addClass("btn btn-default")
                .append($("<span></span>").addClass("glyphicon glyphicon-play"));
            var span = $("<span></span>").text(" " + value["id"]);
            listGroupHeading.append(button);
            listGroupHeading.append(span);
            listGroupItem.append(listGroupHeading);
            listGroupItem.append($("<div></div>").addClass("list-group-item").text(value["description"]));

            var responseDiv = $("<div></div>").addClass("list-group-item");
            listGroupItem.append(responseDiv);
            listGroupGroups.append(listGroupItem);

            button.click(function () {
                runGroup(value["id"], responseDiv)
            });
        });

        $("#groups").append(listGroupGroups);
    } else {
        $("#groups").append($("<div></div>").text("There are no groups."));
    }
}

/**
 *  Toggle the visibility of the details elements.
 *
 *  @param button the button the fired the event
 *  @param detailsId the ID of the <div/> that should be toggled
 */
function toggleDetails(button, detailsId) {

    console.log("toggleDetails(..., " + detailsId + ")");

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

    $("#" + detailsId).toggle();
}

/**
 * Run the concept with the given ID.
 * @param conceptId the ID of the concept
 * @param responseDiv the DIV where the response should be rendered into
 */
function runConcept(conceptId, responseDiv) {

    console.log("runConcept(" + conceptId + ")");

    responseDiv.text("");
    responseDiv.attr("class", "list-group-item"); // reset the CSS of the DIV

    showSpinner();

    $.ajax(JqaConstants.REST_ANALYSIS_CONCEPT_URL,
        {
            'data': conceptId,
            'type': 'POST',
            'headers': {
                'Accepts': 'text/plain',
                'Content-Type': 'text/plain'
            }
        }).done(function (data, textStatus, jqXHR) { /* the success function */

            console.log("Successfully ran concept.");

            var message;
            var listClass;
            if (!data) {

                if (jqXHR.status == 304) {
                    message = "Concept has already been executed on the server.";
                    listClass = "list-group-item-info";
                } else {
                    // there was an error in the call (or at server side)
                    message = "No result received from the server. Please check the logs.";
                    listClass = "list-group-item-warning";
                }
            } else {
                listClass = "list-group-item-success";
                message = "Affected rows after executing the concept: " + data;
            }

            responseDiv.addClass(listClass);
            responseDiv.text(message);

        })
        .fail(function (jqxhr, textStatus, error) { /* the failed function */

            var errorMessage;
            if (jqxhr.status == 0) {
                errorMessage = "Error running concept: Is the service down?";
            } else {
                errorMessage = "Error running concept: " + jqxhr.status + " " + jqxhr.statusText;
            }
            console.log(errorMessage);

            responseDiv.addClass("list-group-item-danger");
            responseDiv.text(errorMessage);
        })
        .always(function () { /* always executed after the AJAX call */
            removeSpinner();
            console.log("Finished running concept.");
        });
}

/**
 * Run the constraint with the given ID.
 * @param constraintId the ID of the constraint
 * @param responseDiv the DIV where the response should be rendered into
 */
function runConstraint(constraintId, responseDiv) {

    console.log("runConstraint(" + constraintId + ")");

    responseDiv.text("");
    responseDiv.attr("class", "list-group-item"); // reset the CSS of the DIV

    showSpinner();

    $.ajax(JqaConstants.REST_ANALYSIS_CONSTRAINT_URL,
        {
            'data': constraintId,
            'type': 'POST',
            'headers': {
                'Accepts': 'text/plain',
                'Content-Type': 'text/plain'
            }
        }).done(function (data, textStatus, jqXHR) { /* the success function */

            console.log("Successfully ran constraint.");

            var message;
            var listClass;
            if (!data) {
                message = "No result received from the server. Please check the logs.";
                listClass = "list-group-item-warning";
            } else {
                message = "Affected rows after executing the constraint: " + data;
                listClass = "list-group-item-success";
            }

            responseDiv.addClass(listClass);
            responseDiv.text(message);

            responseDiv.show();

        })
        .fail(function (jqxhr, textStatus, error) { /* the failed function */

            var errorMessage;
            if (jqxhr.status == 0) {
                errorMessage = "Error running constraint: Is the service down?";
            } else {
                errorMessage = "Error running constraint: " + jqxhr.status + " " + jqxhr.statusText;
            }
            console.log(errorMessage);

            responseDiv.addClass("list-group-item-danger");
            responseDiv.text(errorMessage);

            responseDiv.show();
        })
        .always(function () { /* always executed after the AJAX call */
            removeSpinner();
            console.log("Finished running constraint.");
        });
}

/**
 * Run the group with the given ID.
 * @param groupId the ID of the constraint
 * @param responseDiv the DIV where the response should be rendered into
 */
function runGroup(groupId, responseDiv) {

    console.log("runGroup(" + groupId + ")");

    responseDiv.text("");
    responseDiv.attr("class", "list-group-item"); // reset the CSS of the DIV

    showSpinner();

    $.ajax(JqaConstants.REST_ANALYSIS_GROUP_URL,
        {
            'data': groupId,
            'type': 'POST',
            'headers': {
                'Accepts': 'text/plain',
                'Content-Type': 'text/plain'
            }
        }).done(function (data, textStatus, jqXHR) { /* the success function */

            console.log("Successfully ran group.");

            var message;
            var listClass;
            if (!data) {
                message = "No result received from the server. Please check the logs.";
                listClass = "list-group-item-warning";
            } else {
                message = "Affected rows after executing the group: " + data;
                listClass = "list-group-item-success";
            }

            responseDiv.addClass(listClass);
            responseDiv.text(message);

            responseDiv.show();

        })
        .fail(function (jqxhr, textStatus, error) { /* the failed function */

            var errorMessage;
            if (jqxhr.status == 0) {
                errorMessage = "Error running group: Is the service down?";
            } else {
                errorMessage = "Error running group: " + jqxhr.status + " " + jqxhr.statusText;
            }
            console.log(errorMessage);

            responseDiv.addClass("list-group-item-danger");
            responseDiv.text(errorMessage);

            responseDiv.show();
        })
        .always(function () { /* always executed after the AJAX call */
            removeSpinner();
            console.log("Finished running group.");
        });
}
