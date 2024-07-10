package com.buschmais.jqassistant.core.rule.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class DescriptionTest {

    @Mock
    private Rule ruleConfiguration;

    private List<RuleSet> ruleSets;

    @BeforeEach
    void readRuleSets() throws RuleException {
        List<RuleSet> ruleSets = new ArrayList<>();
        ruleSets.add(RuleSetTestHelper.readRuleSet("/yaml/descriptions.yaml", ruleConfiguration));
        ruleSets.add(RuleSetTestHelper.readRuleSet("/descriptions.xml", ruleConfiguration));
        this.ruleSets = ruleSets;
    }

    @ParameterizedTest
    @MethodSource
    void getIdAndResultOfConcepts(String id, String expected) throws RuleException {
        for (RuleSet ruleSet : ruleSets) {
            Concept concept = ruleSet.getConceptBucket().getById(id);
            assertThat(concept.getDescription()).isEqualTo(expected);
        }
    }

    public static Stream<Arguments> getIdAndResultOfConcepts() {
        return Stream.of(
                Arguments.of("test:WithoutDescription", null),
                Arguments.of("test:EmptyDescription", ""),
                Arguments.of("test:SingleLineDescription", "Cal took a long, deep breath, struggling to control his own emotions."),
                Arguments.of("test:MultiMixEmptyLineDescription", "Cal took a long, deep breath,\n\nstruggling to control his own emotions."));
    }

    @ParameterizedTest
    @MethodSource
    void getIdAndResultOfConstraints(String id, String expected) throws RuleException {
        for (RuleSet ruleSet : ruleSets) {
            Constraint constraint = ruleSet.getConstraintBucket().getById(id);
            assertThat(constraint.getDescription()).isEqualTo(expected);
        }
    }

    public static Stream<Arguments> getIdAndResultOfConstraints() {
        return Stream.of(
                Arguments.of("test:WithoutDescription", null),
                Arguments.of("test:EmptyDescription", ""),
                Arguments.of("test:SingleLineDescription", "When he arrived at the station, the bus had already left."),
                Arguments.of("test:MultiMixEmptyLineDescription", "When he arrived at the station,\n\nthe bus had already left."));
    }

}
