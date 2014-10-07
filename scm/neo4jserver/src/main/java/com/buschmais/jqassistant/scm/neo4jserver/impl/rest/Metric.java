package com.buschmais.jqassistant.scm.neo4jserver.impl.rest;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Metric {

    private String id;

    private String description;

    private String query;

    private List<String> parameters = new ArrayList<String>();

    private String requiredConcept;

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

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public String getRequiredConcept() {
        return requiredConcept;
    }

    public void setRequiredConcept(String requiredConcept) {
        this.requiredConcept = requiredConcept;
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
        object.put("query", query);
        object.put("requiredConcept", requiredConcept);

        JSONArray parameterArray = new JSONArray(parameters);
        object.put("parameters", parameterArray);

        return object;
    }

}
