package com.buschmais.jqassistant.scm.neo4jserver.impl.rest;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MetricGroup {

    private String id;

    private String description;

    private List<Metric> metrics = new ArrayList<Metric>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }

    /**
     * Convert this object into a JSON object.
     * 
     * @return the JSON object
     * @throws JSONException
     */
    public JSONObject asJsonObject() throws JSONException {

        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("description", description);

        JSONArray metricsArray = new JSONArray();
        for (Metric metric : metrics) {
            metricsArray.put(metric.asJsonObject());
        }
        object.put("metrics", metricsArray);

        return object;
    }

}
