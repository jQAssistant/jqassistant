package com.buschmais.jqassistant.scm.neo4jserver.impl.rest;

import java.util.Arrays;
import java.util.Collections;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.buschmais.jqassistant.core.pluginrepository.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.store.api.Store;

@Path("/analysis")
public class AnalysisRestService extends AbstractJQARestService {

    public AnalysisRestService(@Context Store store) throws PluginRepositoryException {
        super(store);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rule")
    public Response getRuleSet() {
        try {
            return Response.status(Response.Status.OK).entity(getAvailableRules()).build();
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
            store.start(getScannerPluginRepository().getDescriptorTypes());
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

}
