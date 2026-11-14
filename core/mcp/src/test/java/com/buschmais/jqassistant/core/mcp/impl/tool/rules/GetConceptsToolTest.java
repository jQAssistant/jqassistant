package com.buschmais.jqassistant.core.mcp.impl.tool.rules;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.mcp.impl.tool.rules.output.Concept;
import com.buschmais.jqassistant.core.mcp.impl.tool.rules.output.GetConceptsResult;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static com.buschmais.jqassistant.core.rule.api.model.Concept.Activation.IF_AVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the {@link GetConceptsTool}.
 */
class GetConceptsToolTest {

    private static final RuleSource RULE_SOURCE = new FileRuleSource(new File("."), "test.xml");

    private GetConceptsTool getConceptsTool;

    @BeforeEach
    void setUp() throws RuleException {
        // java:Type is provided by java:TestClass (concept) and by spring:Component (group)
        com.buschmais.jqassistant.core.rule.api.model.Concept javaType = com.buschmais.jqassistant.core.rule.api.model.Concept.builder()
            .id("java:Type")
            .description("Labels all types.")
            .executable(new CypherExecutable("MATCH (t:Type) RETURN t"))
            .ruleSource(RULE_SOURCE)
            .build();
        com.buschmais.jqassistant.core.rule.api.model.Concept javaTestClass = com.buschmais.jqassistant.core.rule.api.model.Concept.builder()
            .id("java:TestClass")
            .description("Labels test classes.")
            .executable(new CypherExecutable("MATCH (t:Type:Test) RETURN t"))
            .requiresConcepts(Map.of("java:Type", false, "junit*:TestMethod", true))
            .providedConcept(providedConcept("java:TestClass", "java:Type"))
            .ruleSource(RULE_SOURCE)
            .build();
        com.buschmais.jqassistant.core.rule.api.model.Concept springComponent = com.buschmais.jqassistant.core.rule.api.model.Concept.builder()
            .id("spring:Component")
            .executable(new ScriptExecutable("javascript", "var x = 1;"))
            .providedConcept(providedConcept("spring:Component", "java:Type"))
            .ruleSource(RULE_SOURCE)
            .build();
        com.buschmais.jqassistant.core.rule.api.model.Concept springController = com.buschmais.jqassistant.core.rule.api.model.Concept.builder()
            .id("spring:Controller")
            .ruleSource(RULE_SOURCE)
            .build();
        Group group = Group.builder()
            .id("spring:Default")
            .providedConcepts(Map.of("java:TestClass", Set.of(providedConcept("spring:Component", "java:TestClass"))))
            .ruleSource(RULE_SOURCE)
            .build();
        RuleSet ruleSet = RuleSetBuilder.newInstance()
            .addConcept(javaType)
            .addConcept(javaTestClass)
            .addConcept(springComponent)
            .addConcept(springController)
            .addGroup(group)
            .getRuleSet();
        getConceptsTool = new GetConceptsTool(ruleSet, new JacksonMcpJsonMapper(JsonMapper.shared()));
    }

    @Test
    void toolMetadata() {
        assertThat(getConceptsTool.getName()).isEqualTo("get-concepts");
        assertThat(getConceptsTool.getDescription()).isNotBlank();
        assertThat(getConceptsTool.getInputSchema()).isNotBlank();
        assertThat(getConceptsTool.getOutputSchema()).isPresent();
        assertThat(getConceptsTool.getAnnotations()
            .readOnlyHint()).isTrue();
        assertThat(getConceptsTool.getAnnotations()
            .destructiveHint()).isFalse();
        assertThat(getConceptsTool.getAnnotations()
            .idempotentHint()).isTrue();
    }

    @Test
    void allConceptsWithoutArguments() {
        List<Concept> concepts = execute(Map.of());

        assertThat(concepts).extracting(Concept::getId)
            .containsExactlyInAnyOrder("java:Type", "java:TestClass", "spring:Component", "spring:Controller");
        assertThat(concepts).extracting(Concept::getCypher)
            .containsOnlyNulls();
    }

    @Test
    void description() {
        assertThat(getConcept(execute(Map.of()), "java:Type").getDescription()).isEqualTo("Labels all types.");
        assertThat(getConcept(execute(Map.of()), "spring:Controller").getDescription()).isNull();
    }

    @Test
    void filterByExactId() {
        List<Concept> concepts = execute(Map.of("filter", Map.of("ids", List.of("java:Type"))));

        assertThat(concepts).extracting(Concept::getId)
            .containsExactly("java:Type");
    }

    @Test
    void filterByWildcards() {
        assertThat(execute(Map.of("filter", Map.of("ids", List.of("java:*"))))).extracting(Concept::getId)
            .containsExactlyInAnyOrder("java:Type", "java:TestClass");
        assertThat(execute(Map.of("filter", Map.of("ids", List.of("java:T?pe"))))).extracting(Concept::getId)
            .containsExactly("java:Type");
    }

    @Test
    void filterByMultipleIds() {
        List<Concept> concepts = execute(Map.of("filter", Map.of("ids", List.of("java:Type", "spring:*"))));

        assertThat(concepts).extracting(Concept::getId)
            .containsExactlyInAnyOrder("java:Type", "spring:Component", "spring:Controller");
    }

    @Test
    void filterWithoutMatch() {
        assertThat(execute(Map.of("filter", Map.of("ids", List.of("unknown:*"))))).isEmpty();
    }

    @Test
    void filterWithoutIds() {
        assertThat(execute(Map.of("filter", Map.of()))).hasSize(4);
    }

    @Test
    void includeCypher() {
        List<Concept> concepts = execute(Map.of("include", Map.of("cypher", true)));

        assertThat(getConcept(concepts, "java:Type").getCypher()).isEqualTo("MATCH (t:Type) RETURN t");
        assertThat(getConcept(concepts, "java:TestClass").getCypher()).isEqualTo("MATCH (t:Type:Test) RETURN t");
        // non-cypher executable
        assertThat(getConcept(concepts, "spring:Component").getCypher()).isNull();
        // no executable
        assertThat(getConcept(concepts, "spring:Controller").getCypher()).isNull();
    }

    @Test
    void excludeCypher() {
        List<Concept> concepts = execute(Map.of("include", Map.of("cypher", false)));

        assertThat(concepts).extracting(Concept::getCypher)
            .containsOnlyNulls();
    }

    @Test
    void providesConcepts() {
        List<Concept> concepts = execute(Map.of());

        assertThat(getConcept(concepts, "java:TestClass").getProvidesConcepts()).containsExactly("java:Type");
        // provided by the concept itself and by the group, sorted by id
        assertThat(getConcept(concepts, "spring:Component").getProvidesConcepts()).containsExactly("java:TestClass", "java:Type");
        assertThat(getConcept(concepts, "java:Type").getProvidesConcepts()).isEmpty();
        assertThat(getConcept(concepts, "spring:Controller").getProvidesConcepts()).isEmpty();
    }

    @Test
    void requiresConcepts() {
        List<Concept> concepts = execute(Map.of());

        // sorted by id, wildcards are preserved
        assertThat(getConcept(concepts, "java:TestClass").getRequiresConcepts()).containsExactly("java:Type", "junit*:TestMethod");
        assertThat(getConcept(concepts, "java:Type").getRequiresConcepts()).isEmpty();
    }

    private List<Concept> execute(Map<String, Object> arguments) {
        return ((GetConceptsResult) getConceptsTool.execute(arguments)).getConcepts();
    }

    private static Concept getConcept(List<Concept> concepts, String id) {
        return concepts.stream()
            .filter(concept -> concept.getId()
                .equals(id))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Concept " + id + " not found"));
    }

    private static com.buschmais.jqassistant.core.rule.api.model.Concept.ProvidedConcept providedConcept(String providingConceptId,
        String providedConceptId) {
        return com.buschmais.jqassistant.core.rule.api.model.Concept.ProvidedConcept.builder()
            .providingConceptId(providingConceptId)
            .providedConceptId(providedConceptId)
            .activation(IF_AVAILABLE)
            .build();
    }
}
