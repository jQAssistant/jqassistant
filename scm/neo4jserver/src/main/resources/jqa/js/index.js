'use strict';

/**
 *  Get the version.
 */
function getVersion() {

    console.log("getVersion()");

    var url = JqaConstants.REST_VERSION_URL;

    console.log("Loading version from " + url);

    $.get(url)
        .done(function (data) { /* the success function */
            console.log("Successfully got version.");

            $("#version").text(data);
        })
        .fail(function (jqxhr, textStatus, error) { /* the failed function */

            var errorMessage = "Error getting version: " + jqxhr.status + " " + jqxhr.statusText;
            console.log(errorMessage);
        })
        .always(function () { /* always executed after the AJAX call */
            console.log("Finished getting version.");
        });
}
