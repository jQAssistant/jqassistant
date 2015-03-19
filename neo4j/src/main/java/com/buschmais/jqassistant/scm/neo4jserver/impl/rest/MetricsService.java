package com.buschmais.jqassistant.scm.neo4jserver.impl.rest;

import java.text.MessageFormat;
import java.util.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.api.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.CypherExecutable;
import com.buschmais.jqassistant.core.analysis.api.rule.Executable;
import com.buschmais.jqassistant.core.analysis.api.rule.Metric;
import com.buschmais.jqassistant.core.analysis.api.rule.MetricGroup;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;

@Path("/metrics")
public class MetricsService extends AbstractJQARestService {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsService.class);

    /** The parameter key for the metric group. */
    private static final String PARAMETER_GROUP_METRIC_ID = "groupMetricId";
    /** The parameter key for the metric. */
    private static final String PARAMETER_METRIC_ID = "metricId";

    /** The JSON object key for the results array. */
    private static final String JSON_OBJECT_KEY_RESULT = "result";
    /** The JSON object key for the error message. */
    private static final String JSON_OBJECT_KEY_ERRORS = "error";
    /** The JSON object key for columns. */
    private static final String JSON_OBJECT_KEY_COLUMNS = "columns";
    /** The JSON object key for rows. */
    private static final String JSON_OBJECT_KEY_ROW = "row";
    /** The JSON object key for data. */
    private static final String JSON_OBJECT_KEY_DATA = "data";
    /** The JSON object key for {@value} . */
    private static final String JSON_OBJECT_KEY_ID = "id";
    /** The JSON object key for {@value} . */
    private static final String JSON_OBJECT_KEY_DESCRIPTION = "description";
    /** The JSON object key for {@value} . */
    private static final String JSON_OBJECT_KEY_CYPHER = "cypher";
    /** The JSON object key for {@value} . */
    private static final String JSON_OBJECT_KEY_PARAMETERS = "parameters";
    /** The JSON object key for {@value} . */
    private static final String JSON_OBJECT_KEY_METRICS = "metrics";

    /**
     * Constructs a new service.
     *
     * @param store
     *            the database store
     * @throws PluginRepositoryException
     */
    public MetricsService(@Context Store store) throws PluginRepositoryException, RuleException {
        super(store);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public JSONArray getMetrics() {

        JSONArray metrics = new JSONArray();
        try {
            for (MetricGroup metricGroup : readMetricGroups().values()) {
                metrics.put(metricGroupAsJsonObject(metricGroup));
            }
        } catch (JSONException e) {
            throw new WebApplicationException(e);
        }
        return metrics;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/run")
    public Response executeMetric(@Context UriInfo uriInfo) {

        MultivaluedMap<String, String> uriParameters = uriInfo.getQueryParameters();
        String groupId = uriParameters.getFirst(PARAMETER_GROUP_METRIC_ID);
        String metricId = uriParameters.getFirst(PARAMETER_METRIC_ID);

        Metric metric = findMetric(groupId, metricId);

        if (metric != null) {

            // get the required concepts
            List<String> conceptIds = new ArrayList<>();
            for (String conceptId : metric.getRequiresConcepts()) {
                conceptIds.add(conceptId);
            }

            // run the concepts - if there are some
            if (!conceptIds.isEmpty()) {
                try {
                    analyze(conceptIds, Collections.<String> emptyList(), Collections.<String> emptyList());
                } catch (Exception e) {
                    LOGGER.error("Executing concepts (" + conceptIds + ") for metric (" + groupId + "/" + metricId + ") failed.", e);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity((e.getMessage())).build();
                }
            }

            // create the parameter map for running the metric query
            Map<String, Object> queryParameters = new HashMap<>();
            for (String queryParameter : metric.getParameterTypes().keySet()) {
                queryParameters.put(queryParameter, uriParameters.getFirst(queryParameter));
            }

            Store store = getStore();

            // run the metric query
            Executable executable = metric.getExecutable();
            Result<CompositeRowObject> queryResult = store.executeQuery(((CypherExecutable) executable).getStatement(), queryParameters);

            // return the result
            try {
                JSONObject jResponse = createJsonResponse(queryResult);
                return Response.status(Response.Status.OK).entity(jResponse.toString()).build();
            } catch (JSONException e) {
                throw new WebApplicationException(e);
            }
        } else {

            try {
                String message = MessageFormat.format("Unable to find metric for group id '{0}' and metric id '{1}'.", groupId, metricId);
                return Response.status(Response.Status.OK).entity(createJsonError(message).toString()).build();
            } catch (JSONException e) {
                throw new WebApplicationException(e);
            }
        }
    }

    /**
     * Find the metric identified by the given group and metric id.
     * 
     * @param metricGroupId
     *            the group id to look for
     * @param metricId
     *            the metric id to look for
     * @return the metric or {@code null} if none was found
     */
    private Metric findMetric(String metricGroupId, String metricId) {

        MetricGroup metricGroup = readMetricGroups().get(metricGroupId);
        if (metricGroup != null) {
            return metricGroup.getMetrics().get(metricId);
        }

        return null;
    }

    /**
     * Create a JSON error object.
     * 
     * @param errorMessage
     *            the error message to place in the object
     * @return the JSON error object
     * @throws JSONException
     */
    private JSONObject createJsonError(String errorMessage) throws JSONException {

        JSONObject jResponse = new JSONObject();

        // the error message
        jResponse.put(JSON_OBJECT_KEY_ERRORS, errorMessage);

        return jResponse;
    }

    /**
     * Creates the JSON response object from the query result.
     * 
     * @param queryResult
     *            the result of the query
     * @return the JSON response object
     * @throws JSONException
     */
    private JSONObject createJsonResponse(Result<CompositeRowObject> queryResult) throws JSONException {

        if (queryResult == null || !queryResult.hasResult()) {
            return createJsonError("Executing the metric returned empty result.");
        }

        JSONObject response = new JSONObject();

        JSONObject result = new JSONObject();
        response.put(JSON_OBJECT_KEY_RESULT, result);

        JSONArray columns = new JSONArray();
        result.put(JSON_OBJECT_KEY_COLUMNS, columns);
        boolean columnsSet = false;

        JSONArray data = new JSONArray();
        result.put(JSON_OBJECT_KEY_DATA, data);

        for (CompositeRowObject queryRow : queryResult) {

            JSONObject dataObject = new JSONObject();

            JSONArray row = new JSONArray();
            dataObject.put(JSON_OBJECT_KEY_ROW, row);

            Collection<String> queryColumns = queryRow.getColumns();
            for (String column : queryColumns) {
                if (!columnsSet) {
                    columns.put(column);
                }
                row.put(queryRow.get(column, Object.class));
            }
            columnsSet = true;

            data.put(dataObject);
        }

        return response;
    }

    /**
     * Convert a given metric group object into a JSON object.
     * 
     * @param metricGroup
     *            the metric group object to convert
     * @return a JSON object
     * @throws JSONException
     */
    private JSONObject metricGroupAsJsonObject(MetricGroup metricGroup) throws JSONException {

        JSONObject object = new JSONObject();
        object.put(JSON_OBJECT_KEY_ID, metricGroup.getId());
        object.put(JSON_OBJECT_KEY_DESCRIPTION, metricGroup.getDescription());

        JSONArray metricsArray = new JSONArray();
        for (Metric metric : metricGroup.getMetrics().values()) {

            JSONObject metricObject = new JSONObject();
            metricObject.put(JSON_OBJECT_KEY_ID, metric.getId());
            metricObject.put(JSON_OBJECT_KEY_DESCRIPTION, metric.getDescription());
            metricObject.put(JSON_OBJECT_KEY_CYPHER, ((CypherExecutable) metric.getExecutable()).getStatement());

            JSONArray parameterArray = new JSONArray();
            for (String parameterKey : metric.getParameterTypes().keySet()) {
                parameterArray.put(parameterKey);
            }

            metricObject.put(JSON_OBJECT_KEY_PARAMETERS, parameterArray);

            metricsArray.put(metricObject);
        }
        object.put(JSON_OBJECT_KEY_METRICS, metricsArray);

        return object;
    }

    /**
     * Reads the available metric groups.
     * 
     * @return the map of metric groups
     */
    private Map<String, MetricGroup> readMetricGroups() {

        return getAvailableRules().getMetricGroups();
    }

}
