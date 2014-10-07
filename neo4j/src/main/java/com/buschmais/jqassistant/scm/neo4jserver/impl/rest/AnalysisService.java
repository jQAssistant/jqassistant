package com.buschmais.jqassistant.scm.neo4jserver.impl.rest;

import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.store.api.Store;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/analysis")
public class AnalysisService
        extends AbstractJQARestService {

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
    /** The JSON object key for {@value} . */
    private static final String JSON_OBJECT_KEY_MISSING_CONCEPTS = "missingConcepts";
    /** The JSON object key for {@value} . */
    private static final String JSON_OBJECT_KEY_MISSING_CONSTRAINTS = "missingConstraints";
    /** The JSON object key for {@value} . */
    private static final String JSON_OBJECT_KEY_MISSING_GROUPS = "missingGroups";

    public AnalysisService(@Context Store store) throws PluginRepositoryException {
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
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity((e.getMessage())).build();
        }
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/concept")
    public Response runConcept(String conceptId) {
        InMemoryReportWriter report;
        Store store = getStore();
        try {
            store.start(getModelPluginRepository().getDescriptorTypes());
            report = analyze(store, Arrays.asList(conceptId), Collections.<String> emptyList(), Collections.<String> emptyList());

            int conceptResultSize = report.getConceptResults().size();

            if (conceptResultSize == 0) { // nothing modified
                return Response.status(Response.Status.NOT_MODIFIED).build();
            } else {
                return Response.status(Response.Status.OK).entity((Integer.toString(conceptResultSize))).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity((e.getMessage())).build();
        } finally {
            store.stop();
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
            conceptObject.put(JSON_OBJECT_KEY_CYPHER, concept.getValue().getQuery().getCypher());
            concepts.put(conceptObject);
        }

        /*
        JSONObject conceptObject = new JSONObject();
        conceptObject.put(JSON_OBJECT_KEY_ID, "concept-id-00");
        conceptObject.put(JSON_OBJECT_KEY_DESCRIPTION, "some meaningful description");
        conceptObject.put(JSON_OBJECT_KEY_CYPHER, "match (a) return a;");
        concepts.put(conceptObject);

        JSONObject conceptObject1 = new JSONObject();
        conceptObject1.put(JSON_OBJECT_KEY_ID, "concept-id-01");
        conceptObject1.put(JSON_OBJECT_KEY_DESCRIPTION, "some meaningful description");
        conceptObject1.put(JSON_OBJECT_KEY_CYPHER, "match (b) return c;");
        concepts.put(conceptObject1);
        */

        JSONArray constraints = new JSONArray();
        response.put(JSON_OBJECT_KEY_CONSTRAINTS, constraints);
        for (Map.Entry<String, Constraint> constraint : ruleSet.getConstraints().entrySet()) {
            JSONObject constraintObject = new JSONObject();
            constraintObject.put(JSON_OBJECT_KEY_ID, constraint.getValue().getId());
            constraintObject.put(JSON_OBJECT_KEY_DESCRIPTION, constraint.getValue().getDescription());
            constraintObject.put(JSON_OBJECT_KEY_CYPHER, constraint.getValue().getQuery().getCypher());
            constraints.put(constraintObject);
        }

        /*
        JSONObject constraintObject = new JSONObject();
        constraintObject.put(JSON_OBJECT_KEY_ID, "constraint-id-00");
        constraintObject.put(JSON_OBJECT_KEY_DESCRIPTION, "some meaningful description");
        constraintObject.put(JSON_OBJECT_KEY_CYPHER, "match a with a return a;");
        constraints.put(constraintObject);
        */

        JSONArray groups = new JSONArray();
        response.put(JSON_OBJECT_KEY_GROUPS, groups);
        for (Map.Entry<String, Group> group : ruleSet.getGroups().entrySet()) {
            JSONObject groupObject = new JSONObject();
            groupObject.put(JSON_OBJECT_KEY_ID, group.getValue().getId());
            groupObject.put(JSON_OBJECT_KEY_DESCRIPTION, group.getValue().getDescription());
            groups.put(groupObject);
        }

        /*
        JSONObject groupObject = new JSONObject();
        groupObject.put(JSON_OBJECT_KEY_ID, "group-id-00");
        groupObject.put(JSON_OBJECT_KEY_DESCRIPTION, "some meaningful description");
        groups.put(groupObject);
        */

        JSONArray missingConcepts = new JSONArray();
        response.put(JSON_OBJECT_KEY_MISSING_CONCEPTS, missingConcepts);
        for (String missingConcept : ruleSet.getMissingConcepts()) {
            missingConcepts.put(missingConcept);
        }

        /*
        missingConcepts.put("missing-concept-id-00");
        missingConcepts.put("missing-concept-id-01");
        */

        JSONArray missingConstraints = new JSONArray();
        response.put(JSON_OBJECT_KEY_MISSING_CONSTRAINTS, missingConstraints);
        for (String missingConstraint : ruleSet.getMissingConstraints()) {
            missingConstraints.put(missingConstraint);
        }

        JSONArray missingGroups = new JSONArray();
        response.put(JSON_OBJECT_KEY_MISSING_GROUPS, missingConstraints);
        for (String missingGroup : ruleSet.getMissingGroups()) {
            missingGroups.put(missingGroup);
        }

        return response;
    }
}
