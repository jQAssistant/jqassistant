package com.buschmais.jqassistant.core.mcp.impl.tool.store;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.mcp.impl.tool.store.output.ExecuteCypherResult;
import com.buschmais.jqassistant.core.mcp.impl.tool.store.output.Node;
import com.buschmais.jqassistant.core.mcp.impl.tool.store.output.Relationship;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.jqassistant.core.report.api.model.source.SourceLocation;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.CompositeObject;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;
import com.buschmais.xo.api.XOException;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.neo4j.api.model.Neo4jLabel;
import com.buschmais.xo.neo4j.api.model.Neo4jNode;
import com.buschmais.xo.neo4j.api.model.Neo4jRelationship;
import com.buschmais.xo.neo4j.api.model.Neo4jRelationshipType;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import static com.buschmais.jqassistant.core.mcp.impl.tool.store.ExecuteCypherTool.DEFAULT_MAX_ROWS;
import static com.buschmais.jqassistant.core.mcp.impl.tool.store.output.ExecuteCypherResult.Status.ERROR;
import static com.buschmais.jqassistant.core.mcp.impl.tool.store.output.ExecuteCypherResult.Status.SUCCESS;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Verifies the {@link ExecuteCypherTool}.
 */
@ExtendWith(MockitoExtension.class)
class ExecuteCypherToolTest {

    private static final String QUERY = "MATCH (n) RETURN n";

    @Mock
    private Store store;

    @Mock
    private XOManagerFactory<?, ?, ?, ?> xoManagerFactory;

    @Mock
    private XOManager xoManager;

    @Mock
    private Query<CompositeRowObject> query;

    @Mock
    private Query.Result<CompositeRowObject> result;

    private ExecuteCypherTool executeCypherTool;

    @BeforeEach
    void setUp() {
        executeCypherTool = new ExecuteCypherTool(store, new JacksonMcpJsonMapper(JsonMapper.shared()));
    }

    @Test
    void toolMetadata() {
        assertThat(executeCypherTool.getName()).isEqualTo("execute-cypher");
        assertThat(executeCypherTool.getDescription()).contains("default: " + DEFAULT_MAX_ROWS);
        assertThat(executeCypherTool.getInputSchema()).contains("\"default\": " + DEFAULT_MAX_ROWS);
        assertThat(executeCypherTool.getOutputSchema()).isPresent();
        assertThat(executeCypherTool.getAnnotations()
            .readOnlyHint()).isFalse();
        assertThat(executeCypherTool.getAnnotations()
            .destructiveHint()).isTrue();
        assertThat(executeCypherTool.getAnnotations()
            .idempotentHint()).isFalse();
    }

    @Test
    void parametersDefaultToEmptyMap() {
        stubQuery(Stream.empty());

        ExecuteCypherResult executeCypherResult = execute(Map.of("query", QUERY));

        verify(query).withParameters(emptyMap());
        assertThat(executeCypherResult.getStatus()).isEqualTo(SUCCESS);
        assertThat(executeCypherResult.getRowCount()).isZero();
        assertThat(executeCypherResult.isTruncated()).isFalse();
        assertThat(executeCypherResult.getRows()).isEmpty();
        assertThat(executeCypherResult.getErrorMessage()).isNull();
    }

    @Test
    void parametersArePassedToQuery() {
        stubQuery(Stream.empty());

        execute(Map.of("query", QUERY, "parameters", Map.of("name", "Foo", "count", 42)));

        verify(query).withParameters(Map.of("name", "Foo", "count", 42));
    }

    @Test
    void defaultMaxRows() {
        AtomicInteger fetchedRows = new AtomicInteger();
        stubQuery(rows(DEFAULT_MAX_ROWS + 10, fetchedRows));

        ExecuteCypherResult executeCypherResult = execute(Map.of("query", QUERY));

        assertThat(executeCypherResult.getStatus()).isEqualTo(SUCCESS);
        assertThat(executeCypherResult.getRowCount()).isEqualTo(DEFAULT_MAX_ROWS);
        assertThat(executeCypherResult.getRows()).hasSize(DEFAULT_MAX_ROWS);
        assertThat(executeCypherResult.isTruncated()).isTrue();
        assertThat(fetchedRows).hasValue(DEFAULT_MAX_ROWS + 1);
    }

    @Test
    void maxRowsNotExceeded() {
        AtomicInteger fetchedRows = new AtomicInteger();
        stubQuery(rows(3, fetchedRows));

        ExecuteCypherResult executeCypherResult = execute(Map.of("query", QUERY, "maxRows", 3));

        assertThat(executeCypherResult.getRowCount()).isEqualTo(3);
        assertThat(executeCypherResult.isTruncated()).isFalse();
        assertThat(executeCypherResult.getRows()).extracting(row -> row.get("i"))
            .containsExactly(0L, 1L, 2L);
    }

    @Test
    void maxRowsExceeded() {
        AtomicInteger fetchedRows = new AtomicInteger();
        stubQuery(rows(5, fetchedRows));

        ExecuteCypherResult executeCypherResult = execute(Map.of("query", QUERY, "maxRows", 3));

        assertThat(executeCypherResult.getRowCount()).isEqualTo(3);
        assertThat(executeCypherResult.isTruncated()).isTrue();
        assertThat(executeCypherResult.getRows()).extracting(row -> row.get("i"))
            .containsExactly(0L, 1L, 2L);
        // only one additional row is fetched to detect truncation
        assertThat(fetchedRows).hasValue(4);
    }

    @Test
    void columnOrderIsPreserved() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("z", 1L);
        values.put("a", 2L);
        values.put("m", 3L);
        stubQuery(Stream.of(row(values)));

        ExecuteCypherResult executeCypherResult = execute(Map.of("query", QUERY));

        assertThat(executeCypherResult.getRows()
            .get(0)).containsExactly(entry("z", 1L), entry("a", 2L), entry("m", 3L));
    }

    @Test
    void xoExceptionOnExecuteReturnsError() {
        stubXOManager();
        when(query.execute()).thenThrow(new XOException("Invalid input 'MATC'"));

        ExecuteCypherResult executeCypherResult = execute(Map.of("query", "MATC (n) RETURN n"));

        assertThat(executeCypherResult.getStatus()).isEqualTo(ERROR);
        assertThat(executeCypherResult.getErrorMessage()).isEqualTo("Invalid input 'MATC'");
        assertThat(executeCypherResult.getRowCount()).isZero();
        assertThat(executeCypherResult.isTruncated()).isFalse();
        assertThat(executeCypherResult.getRows()).isNull();
        verify(xoManager).close();
    }

    @Test
    void xoExceptionWhileIteratingReturnsErrorAndClosesResult() {
        stubQuery(Stream.generate(() -> {
            throw new XOException(null, new IllegalStateException());
        }));

        ExecuteCypherResult executeCypherResult = execute(Map.of("query", QUERY));

        assertThat(executeCypherResult.getStatus()).isEqualTo(ERROR);
        assertThat(executeCypherResult.getErrorMessage()).isEqualTo(XOException.class.getName());
        verify(result).close();
        verify(xoManager).close();
    }

    @Test
    void resultAndXOManagerAreClosed() {
        stubQuery(Stream.empty());

        execute(Map.of("query", QUERY));

        verify(result).close();
        verify(xoManager).close();
    }

    @ParameterizedTest
    @MethodSource("values")
    void mapValue(Object value, Object expectedValue) {
        Map<String, Object> values = new HashMap<>();
        values.put("value", value);
        stubQuery(Stream.of(row(values)));

        ExecuteCypherResult executeCypherResult = execute(Map.of("query", QUERY));

        assertThat(executeCypherResult.getRows()
            .get(0)
            .get("value")).isEqualTo(expectedValue);
    }

    static Stream<Arguments> values() {
        LocalDate date = LocalDate.of(2026, 11, 11);
        Map<Object, Object> map = new LinkedHashMap<>();
        map.put(1, "one");
        map.put("date", date);
        return Stream.of( //
            Arguments.of(null, null), //
            Arguments.of("text", "text"), //
            Arguments.of(42L, 42L), //
            Arguments.of(1.5d, 1.5d), //
            Arguments.of(true, true), //
            Arguments.of(date, "2026-11-11"), //
            Arguments.of(new long[] { 1L, 2L }, List.of(1L, 2L)), //
            Arguments.of(new boolean[] { true, false }, List.of(true, false)), //
            Arguments.of(new String[] { "a", "b" }, List.of("a", "b")), //
            Arguments.of(List.of(1L, date), List.of(1L, "2026-11-11")), //
            Arguments.of(List.of(List.of("nested")), List.of(List.of("nested"))), //
            Arguments.of(map, Map.of("1", "one", "date", "2026-11-11")));
    }

    @Test
    void mapNode() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("name", "Foo");
        properties.put("lines", new int[] { 1, 2 });
        properties.put("created", LocalDate.of(2026, 11, 11));
        CompositeObject compositeNode = compositeNode(properties, "Java", "Type");
        stubQuery(Stream.of(row(Map.of("n", compositeNode))));

        ExecuteCypherResult executeCypherResult;
        try (MockedStatic<ReportHelper> reportHelper = mockStatic(ReportHelper.class)) {
            reportHelper.when(() -> ReportHelper.getSourceLocation(compositeNode))
                .thenReturn(Optional.empty());
            executeCypherResult = execute(Map.of("query", QUERY));
        }

        assertThat(executeCypherResult.getRows()
            .get(0)
            .get("n")).isInstanceOfSatisfying(Node.class, node -> {
            assertThat(node.getKind()).isEqualTo("node");
            assertThat(node.getLabels()).containsExactlyInAnyOrder("Java", "Type");
            assertThat(node.getProperties()).containsExactly(entry("name", "Foo"), entry("lines", List.of(1, 2)), entry("created", "2026-11-11"));
            assertThat(node.getSourceLocation()).isNull();
        });
    }

    @Test
    void mapNodeWithFileLocation() {
        CompositeObject compositeNode = compositeNode(Map.of(), "File");
        stubQuery(Stream.of(row(Map.of("n", compositeNode))));
        FileLocation fileLocation = FileLocation.builder()
            .fileName("Foo.java")
            .path("src/main/java/Foo.java")
            .startLine(Optional.of(10))
            .endLine(Optional.of(20))
            .build();

        ExecuteCypherResult executeCypherResult;
        try (MockedStatic<ReportHelper> reportHelper = mockStatic(ReportHelper.class)) {
            reportHelper.when(() -> ReportHelper.getSourceLocation(compositeNode))
                .thenReturn(Optional.<SourceLocation<?>>of(fileLocation));
            executeCypherResult = execute(Map.of("query", QUERY));
        }

        assertThat(executeCypherResult.getRows()
            .get(0)
            .get("n")).isInstanceOfSatisfying(Node.class, node -> {
            assertThat(node.getSourceLocation()
                .getPath()).isEqualTo("src/main/java/Foo.java");
            assertThat(node.getSourceLocation()
                .getStartLine()).isEqualTo(10);
            assertThat(node.getSourceLocation()
                .getEndLine()).isEqualTo(20);
        });
    }

    @Test
    void mapRelationship() {
        CompositeObject compositeRelationship = compositeRelationship("DECLARES", Map.of("weight", 3L));
        stubQuery(Stream.of(row(Map.of("r", compositeRelationship))));

        ExecuteCypherResult executeCypherResult;
        try (MockedStatic<ReportHelper> ignored = mockStatic(ReportHelper.class)) {
            executeCypherResult = execute(Map.of("query", QUERY));
        }

        assertThat(executeCypherResult.getRows()
            .get(0)
            .get("r")).isInstanceOfSatisfying(Relationship.class, relationship -> {
            assertThat(relationship.getKind()).isEqualTo("relationship");
            assertThat(relationship.getType()).isEqualTo("DECLARES");
            assertThat(relationship.getProperties()).containsExactly(entry("weight", 3L));
            assertThat(relationship.getSourceLocation()).isNull();
        });
    }

    @Test
    void mapPath() {
        CompositeObject start = compositeNode(Map.of(), "Type");
        CompositeObject relationship = compositeRelationship("DECLARES", Map.of());
        CompositeObject end = compositeNode(Map.of(), "Method");
        stubQuery(Stream.of(row(Map.of("p", List.of(start, relationship, end)))));

        ExecuteCypherResult executeCypherResult;
        try (MockedStatic<ReportHelper> ignored = mockStatic(ReportHelper.class)) {
            executeCypherResult = execute(Map.of("query", QUERY));
        }

        assertThat(executeCypherResult.getRows()
            .get(0)
            .get("p")).asInstanceOf(LIST)
            .satisfiesExactly(n -> assertThat(n).isInstanceOf(Node.class), r -> assertThat(r).isInstanceOf(Relationship.class),
                n -> assertThat(n).isInstanceOf(Node.class));
    }

    @Test
    void mapCompositeObjectWithUnknownDelegate() {
        CompositeObject compositeObject = mock(CompositeObject.class);
        doReturn("delegate").when(compositeObject)
            .getDelegate();
        stubQuery(Stream.of(row(Map.of("x", compositeObject))));

        ExecuteCypherResult executeCypherResult;
        try (MockedStatic<ReportHelper> ignored = mockStatic(ReportHelper.class)) {
            executeCypherResult = execute(Map.of("query", QUERY));
        }

        assertThat(executeCypherResult.getRows()
            .get(0)
            .get("x")).isEqualTo("delegate");
    }

    private ExecuteCypherResult execute(Map<String, Object> arguments) {
        return (ExecuteCypherResult) executeCypherTool.execute(arguments);
    }

    private void stubXOManager() {
        doReturn(xoManagerFactory).when(store)
            .getXOManagerFactory();
        when(xoManagerFactory.createXOManager()).thenReturn(xoManager);
        when(xoManager.createQuery(any(String.class))).thenReturn(query);
        when(query.withParameters(any())).thenReturn(query);
    }

    private void stubQuery(Stream<CompositeRowObject> rows) {
        stubXOManager();
        when(query.execute()).thenReturn(result);
        when(result.asStream()).thenReturn(rows);
    }

    /**
     * Creates a lazy stream of rows with a single column "i", the counter tracks the number of rows which have been fetched.
     */
    private static Stream<CompositeRowObject> rows(int count, AtomicInteger fetchedRows) {
        return IntStream.range(0, count)
            .peek(i -> fetchedRows.incrementAndGet())
            .mapToObj(i -> row(Map.of("i", (long) i)));
    }

    private static CompositeRowObject row(Map<String, Object> values) {
        CompositeRowObject row = mock(CompositeRowObject.class);
        when(row.getColumns()).thenReturn(new ArrayList<>(values.keySet()));
        values.forEach((column, value) -> when(row.get(column, Object.class)).thenReturn(value));
        return row;
    }

    private static CompositeObject compositeNode(Map<String, Object> properties, String... labels) {
        Neo4jNode<?, ?, ?, ?> neo4jNode = mock(Neo4jNode.class);
        List<Neo4jLabel> neo4jLabels = Arrays.stream(labels)
            .<Neo4jLabel>map(label -> () -> label)
            .toList();
        doReturn(neo4jLabels).when(neo4jNode)
            .getLabels();
        when(neo4jNode.getProperties()).thenReturn(properties);
        CompositeObject compositeObject = mock(CompositeObject.class);
        doReturn(neo4jNode).when(compositeObject)
            .getDelegate();
        return compositeObject;
    }

    private static CompositeObject compositeRelationship(String type, Map<String, Object> properties) {
        Neo4jRelationship<?, ?, ?, ?, ?> neo4jRelationship = mock(Neo4jRelationship.class);
        Neo4jRelationshipType relationshipType = () -> type;
        doReturn(relationshipType).when(neo4jRelationship)
            .getType();
        when(neo4jRelationship.getProperties()).thenReturn(properties);
        CompositeObject compositeObject = mock(CompositeObject.class);
        doReturn(neo4jRelationship).when(compositeObject)
            .getDelegate();
        return compositeObject;
    }
}
