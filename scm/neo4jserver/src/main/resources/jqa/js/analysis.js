'use strict';

/**
 *  Load the metric groups from the server.
 */
function loadRuleSets() {

    console.log("loadRuleSets()");

    var url = JqaConstants.REST_ANALYSIS_RULES_URL;

    console.log("Loading rule sets from " + url);

    $.getJSON(url)
        .done(function (json) { /* the success function */
            console.log("Successfully loaded rule sets.");

            if (!json) {
                // there was an error in the call (or at server side)
                // TODO beautify this
                alert("No result received from the server. Please check the logs.");
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

            // TODO beautify this
            alert(errorMessage);
        })
        .always(function () { /* always executed after the AJAX call */
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

        var listGroupConcepts = $("<div></div>").addClass("list-group");
        $.each(ruleSets["concepts"], function (index, value) {
            var listGroupItem = $("<div></div>").addClass("list-group-item");
            listGroupItem.append($("<h4></h4>").addClass("list-group-item-heading").text(value["id"]));
            listGroupItem.append($("<div></div>").addClass("list-group-item-heading").text(value["description"]));
            listGroupItem.append($("<div></div>").addClass("list-group-item-text")
                .append($("<pre></pre>").text(value["cypher"])));
            listGroupConcepts.append(listGroupItem);
        });

        $("#concepts").append(listGroupConcepts);
    } else {
        $("#conceptsSize").hide();
        $("#concepts").append($("<div></div>").text("There are no concepts."));
    }

    /*
     *  Constraints
     */
    var constraintsSize = ruleSets["constraints"].length;
    if (constraintsSize > 0) {
        $("#constraintsSize").text(constraintsSize);

        var listGroupConstraints = $("<div></div>").addClass("list-group");
        $.each(ruleSets["constraints"], function (index, value) {
            var listGroupItem = $("<div></div>").addClass("list-group-item");
            listGroupItem.append($("<h4></h4>").addClass("list-group-item-heading").text(value["id"]));
            listGroupItem.append($("<div></div>").addClass("list-group-item-heading").text(value["description"]));
            listGroupItem.append($("<div></div>").addClass("list-group-item-text")
                .append($("<pre></pre>").text(value["cypher"])));
            listGroupConstraints.append(listGroupItem);
        });

        $("#constraints").append(listGroupConstraints);
    } else {
        $("#constraintsSize").hide();
        $("#constraints").append($("<div></div>").text("There are no constraints."));
    }

    /*
     *  Groups
     */
    var groupsSize = ruleSets["groups"].length;
    if (groupsSize > 0) {
        $("#groupsSize").text(groupsSize);

        var listGroupGroups = $("<div></div>").addClass("list-group");
        $.each(ruleSets["groups"], function (index, value) {
            var listGroupItem = $("<div></div>").addClass("list-group-item");
            listGroupItem.append($("<h4></h4>").addClass("list-group-item-heading").text(value["id"]));
            listGroupItem.append($("<div></div>").addClass("list-group-item-heading").text(value["description"]));
            listGroupGroups.append(listGroupItem);
        });

        $("#groups").append(listGroupGroups);
    } else {
        $("#groupsSize").hide();
        $("#groups").append($("<div></div>").text("There are no groups."));
    }

    /*
     *  Missing Concepts
     */
    var missingConceptsSize = ruleSets["missingConcepts"].length;
    if (missingConceptsSize > 0) {
        $("#missingConceptsSize").text(missingConceptsSize);

        var listGroupMissingConcepts = $("<div></div>").addClass("list-group");
        $.each(ruleSets["missingConcepts"], function (index, value) {
            var listGroupItem = $("<div></div>").addClass("list-group-item");
            listGroupItem.append($("<h4></h4>").addClass("list-group-item-heading").text(value));
            listGroupMissingConcepts.append(listGroupItem);
        });

        $("#missingConcepts").append(listGroupMissingConcepts);
    } else {
        $("#missingConceptsSize").hide();
        $("#missingConcepts").append($("<div></div>").text("There are no missing concepts."));
    }

    /*
     *  Missing Constraints
     */
    var missingConstraintsSize = ruleSets["missingConstraints"].length;
    if (missingConstraintsSize > 0) {
        $("#missingConstraintsSize").text(missingConstraintsSize);

        var listGroupMissingConstraints = $("<div></div>").addClass("list-group");
        $.each(ruleSets["missingConstraints"], function (index, value) {
            var listGroupItem = $("<div></div>").addClass("list-group-item");
            listGroupItem.append($("<h4></h4>").addClass("list-group-item-heading").text(value["id"]));
            listGroupItem.append($("<div></div>").addClass("list-group-item-heading").text(value["description"]));
            listGroupItem.append($("<div></div>").addClass("list-group-item-text")
                .append($("<pre></pre>").text(value["cypher"])));
            listGroupMissingConstraints.append(listGroupItem);
        });

        $("#missingConstraints").append(listGroupMissingConstraints);
    } else {
        $("#missingConstraintsSize").hide();
        $("#missingConstraints").append($("<div></div>").text("There are no missing constraints."));
    }

    /*
     *  Missing Groups
     */
    var missingGroupsSize = ruleSets["missingGroups"].length;
    if (missingGroupsSize > 0) {
        $("#missingGroupsSize").text(missingGroupsSize);

        var listGroupMissingGroups = $("<div></div>").addClass("list-group");
        $.each(ruleSets["missingGroups"], function (index, value) {
            var listGroupItem = $("<div></div>").addClass("list-group-item");
            listGroupItem.append($("<h4></h4>").addClass("list-group-item-heading").text(value["id"]));
            listGroupItem.append($("<div></div>").addClass("list-group-item-heading").text(value["description"]));
            listGroupMissingGroups.append(listGroupItem);
        });

        $("#missingGroups").append(listGroupMissingGroups);
    } else {
        $("#missingGroupsSize").hide();
        $("#missingGroups").append($("<div></div>").text("There are no missing groups."));
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
