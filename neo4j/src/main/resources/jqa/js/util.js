/**
 *  An object to hold all constants.
 */
var JqaConstants = {};
JqaConstants.GROUP_ID = 'groupMetricId';
JqaConstants.METRICS_ID = 'metricId';
JqaConstants.REST_BASE_URL = 'http://localhost:7474/jqa/rest';
JqaConstants.REST_METRIC_GROUPS_URL = JqaConstants.REST_BASE_URL + '/metrics';
JqaConstants.REST_RUN_METRIC_URL = JqaConstants.REST_METRIC_GROUPS_URL + '/run';
JqaConstants.REST_ANALYSIS_RULES_URL = JqaConstants.REST_BASE_URL + '/analysis/rules';

/**
 *  A function to get the url parameter value.
 *
 *  @param name the name for which to get the value
 *  @return the value or null
 */
function getUrlHashParameterValue(name) {
    var results = new RegExp('[#&]' + name + '=([^&]*)').exec(location.hash);
    if (results == null) {
        return null;
    }
    else {
        return results[1] || null;
    }
}

/**
 * Get the map of URL hash parameters.
 *
 * @return the map of URL parameters, may be null
 */
function getUrlHashParameters() {

    if (!location.hash || location.hash.length <= 1) {
        // there is no hash
        return null;
    }

    // exclude the "#"
    var hash = location.hash.substr(1, location.hash.length);

    var parameterMap = {};

    $.each(hash.split("&"), function (index, value) {
        var parameterAndValue = value.split("=");
        parameterMap[parameterAndValue[0]] = parameterAndValue[1];
    });

    return parameterMap;
}

/**
 * Set the URL parameter. This removes all existing ones.
 *
 * @param parameterMap the new map of parameters
 */
function setUrlHashParameter(parameterMap) {

    console.log("setUrlParameter(...)");

    var keys = Object.keys(parameterMap);
    if (keys == null) {
        return;
    }

    var urlHash = "";
    for (var i = 0; i < keys.length; i++) {
        urlHash += keys[i];
        urlHash += "=";
        urlHash += parameterMap[keys[i]];

        if ((i + 1) < keys.length) {
            urlHash += "&";
        }
    }

    location.hash = encodeURI(urlHash);
}

/**
 * Get the value for the text color based upon the background color.
 *
 * @param backgroundColor the background color
 * @return the text color (black or white)
 * @see http://24ways.org/2010/calculating-color-contrast/
 */
function getContrastYIQ(backgroundColor){
    if (backgroundColor.indexOf("#") == 0) {
        backgroundColor = backgroundColor.substring(1, backgroundColor.length);
    }
    var r = parseInt(backgroundColor.substr(0,2),16);
    var g = parseInt(backgroundColor.substr(2,2),16);
    var b = parseInt(backgroundColor.substr(4,2),16);
    var yiq = ((r*299)+(g*587)+(b*114))/1000;
    return (yiq >= 128) ? 'black' : 'white';
}
