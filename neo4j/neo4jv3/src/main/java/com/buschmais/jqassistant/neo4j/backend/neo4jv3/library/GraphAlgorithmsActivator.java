package com.buschmais.jqassistant.neo4j.backend.neo4jv3.library;

import java.util.Arrays;

import static java.util.Collections.emptyList;

public class GraphAlgorithmsActivator extends AbstractNeo4jLibraryActivator {

    @Override
    public String getName() {
        return "Graph Algorithms";
    }

    @Override
    protected Iterable<Class<?>> getFunctionTypes() {
        return emptyList();
    }

    @Override
    protected Iterable<Class<?>> getProcedureTypes() {
        return Arrays.asList(org.neo4j.graphalgo.TriangleProc.class, org.neo4j.graphalgo.UtilityProc.class, org.neo4j.graphalgo.similarity.EuclideanProc.class,
                org.neo4j.graphalgo.similarity.JaccardProc.class, org.neo4j.graphalgo.similarity.OverlapProc.class,
                org.neo4j.graphalgo.similarity.CosineProc.class, org.neo4j.graphalgo.similarity.PearsonProc.class,
                org.neo4j.graphalgo.DegreeCentralityProc.class, org.neo4j.graphalgo.InfoMapProc.class, org.neo4j.graphalgo.UnionFindProc.class,
                org.neo4j.graphalgo.MSColoringProc.class, org.neo4j.graphalgo.BalancedTriadsProc.class, org.neo4j.graphalgo.AllShortestPathsProc.class,
                org.neo4j.graphalgo.ShortestPathsProc.class, org.neo4j.graphalgo.PrimProc.class, org.neo4j.graphalgo.UnionFindProc3.class,
                org.neo4j.graphalgo.ArticleRankProc.class, org.neo4j.graphalgo.KSpanningTreeProc.class, org.neo4j.graphalgo.BetweennessCentralityProc.class,
                org.neo4j.graphalgo.ShortestPathDeltaSteppingProc.class, org.neo4j.graphalgo.KShortestPathsProc.class,
                org.neo4j.graphalgo.ShortestPathProc.class, org.neo4j.graphalgo.ListProc.class, org.neo4j.graphalgo.UnionFindProc4.class,
                org.neo4j.graphalgo.HarmonicCentralityProc.class, org.neo4j.graphalgo.EigenvectorCentralityProc.class, org.neo4j.graphalgo.UnionFindProc2.class,
                org.neo4j.graphalgo.LouvainProc.class, org.neo4j.graphalgo.DangalchevCentralityProc.class, org.neo4j.graphalgo.TraverseProc.class,
                org.neo4j.graphalgo.LoadGraphProc.class, org.neo4j.graphalgo.StronglyConnectedComponentsProc.class, org.neo4j.graphalgo.PageRankProc.class,
                org.neo4j.graphalgo.LabelPropagationProc.class, org.neo4j.graphalgo.walking.NodeWalkerProc.class,
                org.neo4j.graphalgo.ClosenessCentralityProc.class);
    }
}
