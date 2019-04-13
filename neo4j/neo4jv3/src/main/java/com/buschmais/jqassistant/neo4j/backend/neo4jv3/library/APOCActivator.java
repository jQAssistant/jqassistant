package com.buschmais.jqassistant.neo4j.backend.neo4jv3.library;

import java.util.List;

import static java.util.Arrays.asList;

public class APOCActivator extends AbstractNeo4jLibraryActivator {

    private final List<Class<?>> PROCEDURE_TYPES = asList(apoc.algo.Cliques.class, apoc.algo.Cover.class, apoc.algo.LabelPropagation.class,
            apoc.algo.PageRank.class, apoc.algo.PathFinding.class, apoc.algo.WeaklyConnectedComponents.class, apoc.atomic.Atomic.class, apoc.bolt.Bolt.class,
            apoc.cache.Static.class, apoc.cluster.Cluster.class, apoc.coll.Coll.class, apoc.config.Config.class, apoc.convert.Json.class,
            apoc.couchbase.Couchbase.class, apoc.create.Create.class, apoc.custom.CypherProcedures.class, apoc.cypher.Cypher.class, apoc.cypher.Timeboxed.class,
            apoc.date.Date.class, apoc.es.ElasticSearch.class, apoc.example.Examples.class, apoc.export.csv.ImportCsv.class, apoc.export.Export.class,
            apoc.export.csv.ExportCSV.class, apoc.export.cypher.ExportCypher.class, apoc.export.graphml.ExportGraphML.class, apoc.export.json.ExportJson.class,
            apoc.generate.Generate.class, apoc.gephi.Gephi.class, apoc.get.Get.class, apoc.graph.Graphs.class, apoc.help.Help.class,
            apoc.index.FreeTextSearch.class, apoc.index.FulltextIndex.class, apoc.index.SchemaIndex.class, apoc.load.LoadHtml.class, apoc.load.Jdbc.class,
            apoc.load.LoadCsv.class, apoc.load.LoadJson.class, apoc.load.LoadLdap.class, apoc.load.LoadXls.class, apoc.load.Xml.class, apoc.lock.Lock.class,
            apoc.log.Logging.class, apoc.log.Neo4jLogStream.class, apoc.math.Regression.class, apoc.merge.Merge.class, apoc.meta.Meta.class,
            apoc.metrics.Metrics.class, apoc.model.Model.class, apoc.mongodb.MongoDB.class, apoc.monitor.Ids.class, apoc.monitor.Kernel.class,
            apoc.monitor.Locks.class, apoc.monitor.Store.class, apoc.monitor.Transaction.class, apoc.neighbors.Neighbors.class, apoc.nodes.Grouping.class,
            apoc.nodes.Nodes.class, apoc.path.PathExplorer.class, apoc.periodic.Periodic.class, apoc.refactor.GraphRefactoring.class,
            apoc.refactor.rename.Rename.class, apoc.schema.Schemas.class, apoc.search.ParallelNodeSearch.class, apoc.spatial.Distance.class,
            apoc.spatial.Geocode.class, apoc.stats.DegreeDistribution.class, apoc.text.Phonetic.class, apoc.trigger.Trigger.class, apoc.util.Utils.class,
            apoc.warmup.Warmup.class);
    private final List<Class<?>> FUNCTION_TYPES = asList(apoc.algo.Similarity.class, apoc.bitwise.BitwiseOperations.class, apoc.coll.Coll.class,
            apoc.convert.Convert.class, apoc.convert.Json.class, apoc.create.Create.class, apoc.cypher.CypherFunctions.class, apoc.data.Extract.class,
            apoc.data.email.ExtractEmail.class, apoc.data.url.ExtractURL.class, apoc.date.Date.class, apoc.diff.Diff.class, apoc.hashing.Fingerprinting.class,
            apoc.label.Label.class, apoc.map.Maps.class, apoc.math.Maths.class, apoc.meta.Meta.class, apoc.nodes.Nodes.class, apoc.number.ArabicRoman.class,
            apoc.number.Numbers.class, apoc.number.exact.Exact.class, apoc.path.Paths.class, apoc.schema.Schemas.class, apoc.scoring.Scoring.class,
            apoc.text.Strings.class, apoc.temporal.TemporalProcedures.class, apoc.trigger.Trigger.class, apoc.util.Utils.class, apoc.version.Version.class);

    @Override
    public String getName() {
        return "APOC";
    }

    @Override
    protected Iterable<Class<?>> getProcedureTypes() {
        return PROCEDURE_TYPES;
    }

    @Override
    protected Iterable<Class<?>> getFunctionTypes() {
        return FUNCTION_TYPES;
    }

}
