package com.buschmais.jqassistant.scm.neo4jserver.impl.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.store.api.Store;

@Path("/analysis")
public class AnalysisService extends AbstractJQARestService {

    /** The JSON object key for {@value} . */
    private static final String JSON_OBJECT_KEY_CONCEPTS = "concepts";
    /** The JSON object key for {@value} . */
    private static final String JSON_OBJECT_KEY_ID = "id";
    /** The JSON object key for {@value} . */
    private static final String JSON_OBJECT_KEY_DESCRIPTION = "description";
    /** The JSON object key for {@value} . */
    private static final String JSON_OBJECT_KEY_CYPHER = "cypher";
    /** The JSON object key for {@value} . */
    private static final String JSON_OBJECT_KEY_CONSTRAINTS = "constraints";
    /** The JSON object key for {@value} . */
    private static final String JSON_OBJECT_KEY_GROUPS = "groups";

    public AnalysisService(@Context Store store) throws PluginRepositoryException, RuleException {
        super(store);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rules")
    public Response getRuleSet() {
        try {
            JSONObject jsonResponse = createJsonResponse(getAvailableRules());
            return Response.status(Response.Status.OK).entity(jsonResponse.toString()).build();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    public String getSomething() {

        return "something";
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/concept")
    public Response runConcept(String conceptId) {

        InMemoryReportWriter report;

        try {
            report = analyze(Arrays.asList(conceptId), Collections.<String> emptyList(), Collections.<String> emptyList());

            Result<Concept> conceptResult = report.getConceptResults().get(conceptId);

            if (conceptResult == null) {
                // the concept was not executed, it already has been executed
                return Response.status(Response.Status.NOT_MODIFIED).build();
            } else {
                int effectedRows = conceptResult.getRows().size();
                return Response.status(Response.Status.OK).entity((Integer.toString(effectedRows))).build();
            }

        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/constraint")
    public Response runConstraint(String constraintId) {

        InMemoryReportWriter report;

        try {
            report = analyze(Collections.<String> emptyList(), Arrays.asList(constraintId), Collections.<String> emptyList());

            int effectedRows = report.getConstraintResults().get(constraintId).getRows().size();
            return Response.status(Response.Status.OK).entity((Integer.toString(effectedRows))).build();

        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/group")
    public Response runGroup(String groupId) {

        InMemoryReportWriter report;

        try {
            report = analyze(Collections.<String> emptyList(), Collections.<String> emptyList(), Arrays.asList(groupId));

            int effectedRows = 0;

            for (Result<Concept> conceptResult : report.getConceptResults().values()) {
                effectedRows += conceptResult.getRows().size();
            }

            for (Result<Constraint> constraintResult : report.getConstraintResults().values()) {
                effectedRows += constraintResult.getRows().size();
            }
            return Response.status(Response.Status.OK).entity((Integer.toString(effectedRows))).build();

        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    /**
     * Create a JSON response object from the given rule set.
     *
     * @param ruleSet
     *            the rule set to use
     * @return the JSON response object
     */
    private JSONObject createJsonResponse(RuleSet ruleSet) throws JSONException {

        JSONObject response = new JSONObject();

        JSONArray concepts = new JSONArray();
        response.put(JSON_OBJECT_KEY_CONCEPTS, concepts);
        for (Map.Entry<String, Concept> concept : ruleSet.getConcepts().entrySet()) {
            JSONObject conceptObject = new JSONObject();
            conceptObject.put(JSON_OBJECT_KEY_ID, concept.getValue().getId());
            conceptObject.put(JSON_OBJECT_KEY_DESCRIPTION, concept.getValue().getDescription());
            conceptObject.put(JSON_OBJECT_KEY_CYPHER, concept.getValue().getCypher());
            concepts.put(conceptObject);
        }

        JSONArray constraints = new JSONArray();
        response.put(JSON_OBJECT_KEY_CONSTRAINTS, constraints);
        for (Map.Entry<String, Constraint> constraint : ruleSet.getConstraints().entrySet()) {
            JSONObject constraintObject = new JSONObject();
            constraintObject.put(JSON_OBJECT_KEY_ID, constraint.getValue().getId());
            constraintObject.put(JSON_OBJECT_KEY_DESCRIPTION, constraint.getValue().getDescription());
            constraintObject.put(JSON_OBJECT_KEY_CYPHER, constraint.getValue().getCypher());
            constraints.put(constraintObject);
        }

        JSONArray groups = new JSONArray();
        response.put(JSON_OBJECT_KEY_GROUPS, groups);
        for (Map.Entry<String, Group> group : ruleSet.getGroups().entrySet()) {
            JSONObject groupObject = new JSONObject();
            groupObject.put(JSON_OBJECT_KEY_ID, group.getValue().getId());
            groupObject.put(JSON_OBJECT_KEY_DESCRIPTION, group.getValue().getDescription());
            groups.put(groupObject);
        }

        return response;
    }
}
