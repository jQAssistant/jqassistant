package com.buschmais.jqassistant.scm.neo4jserver.impl.rest;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.Query.Result;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/metrics")
public class MetricsService extends AbstractJQARestService {

    private static final String DRILLDOWNMETRICS_FILE = "/jqa/drilldown-metrix.json";

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

    /**
     * Constructs a new service.
     *
     * @param store
     *            the database store
     * @throws PluginRepositoryException
     */
    public MetricsService(@Context Store store) throws PluginRepositoryException {
        super(store);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JSONArray getMetrics() {

        JSONArray metrics = new JSONArray();
        try {
            for (MetricGroup metricGroup : readMetricGroups()) {
                metrics.put(metricGroup.asJsonObject());
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

            Map<String, Object> queryParameters = new HashMap<>();
            for (String queryParameter : metric.getParameters()) {
                queryParameters.put(queryParameter, uriParameters.getFirst(queryParameter));
            }

            Store store = getStore();
            Result<CompositeRowObject> queryResult = store.executeQuery(metric.getQuery(), queryParameters);

            try {
                JSONObject jResponse = createJsonResponse(queryResult);
                return Response.status(Response.Status.OK).entity(jResponse.toString()).build();
            } catch (JSONException e) {
                throw new WebApplicationException(e);
            }
        } else {

            try {
                String message = MessageFormat.format("Unable to find metric for group id '{0}' and metric id '{1}'.",
                                                     groupId,
                                                     metricId);
                return Response.status(Response.Status.OK).entity(createJsonError(message).toString()).build();
            }
            catch (JSONException e) {
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

        for (MetricGroup metricGroup : readMetricGroups()) {

            if (!metricGroup.getId().equals(metricGroupId)) {
                continue;
            }

            for (Metric metric : metricGroup.getMetrics()) {
                if (metric.getId().equals(metricId)) {
                    return metric;
                }
            }
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
     * @param queryResult the result of the query
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
     * Reads the available metric groups.
     * 
     * @return a possibly empty list of {@link MetricGroup}, never {@code null}
     */
    private List<MetricGroup> readMetricGroups() {

        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(this.getClass().getResourceAsStream(DRILLDOWNMETRICS_FILE), new TypeReference<ArrayList<MetricGroup>>() {
            });

        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}
