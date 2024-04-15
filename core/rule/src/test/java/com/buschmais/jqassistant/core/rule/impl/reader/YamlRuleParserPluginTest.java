package com.buschmais.jqassistant.core.rule.impl.reader;

import java.net.URL;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.model.Parameter.Type;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YamlRuleParserPluginTest {
    /*
    ** This class contains quite a log of tests. Therefore the test cases are group
    ** by topic. So please keep this if you add new tests.
    **
    ** The name of a test method (not the display name) reflect the topic and
    ** are intended to keep similar tests to gather if you list them in your
    ** idea.
     */

    @Mock
    private Rule rule;

    @Nested
    class ConceptRelated {
        @Test
        void oneConcept() throws Exception {
            RuleSet ruleSet = readRuleSet("/yaml/concept-single-simple.yaml");

            assertThat(ruleSet).isNotNull();
            assertThat(ruleSet.getConceptBucket().size()).isEqualTo(1);
            assertThat(ruleSet.getConstraintBucket().size()).isEqualTo(0);
            assertThat(ruleSet.getGroupsBucket().size()).isEqualTo(0);
        }


        @Test
        void oneConceptWithParameterEmptyListOfParameters() throws Exception {
            RuleSet ruleSet = readRuleSet("/yaml/concept-with-parameter-empty-list-of-parameters.yml");

            assertThat(ruleSet.getConceptBucket().getAll()).hasSize(1);
            Concept concept = ruleSet.getConceptBucket().getAll().stream().findFirst().get();

            assertThat(concept.getParameters()).isEmpty();
        }

        @Test
        void oneConceptExplicitSeverity() throws Exception {
            RuleSet ruleSet = readRuleSet("/yaml/concept-single-with-severity.yaml");

            Concept concept = ruleSet.getConceptBucket().getAll().iterator().next();

            assertThat(concept.getSeverity()).isEqualTo(Severity.BLOCKER);
        }

        @Test
        void oneConceptOneParameter() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/concept-single-with-one-parameter.yaml");

            Concept concept = ruleSet.getConceptBucket().getAll().iterator().next();

            RuleSetTestHelper.verifyParameter(concept.getParameters(), "fqcn", Type.STRING, "foobar");
            assertThat(concept.getParameters()).hasSize(1);
        }

        @Test
        void oneConceptTwoParameters() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/concept-single-with-two-parameters.yaml");

            Concept concept = ruleSet.getConceptBucket().getAll().iterator().next();

            RuleSetTestHelper.verifyParameter(concept.getParameters(), "fqcn", Type.STRING, "foobar");
            RuleSetTestHelper.verifyParameter(concept.getParameters(), "foobar", Type.STRING, null);
            assertThat(concept.getParameters()).hasSize(2);
        }

        @Test
        void oneConceptVerificationAggregation() throws Exception {
            RuleSet ruleSet = readRuleSet("/yaml/concept-single-with-verification-aggrgation.yaml");

            Concept concept = ruleSet.getConceptBucket().getAll().iterator().next();

            assertThat(concept.getVerification()).isNotNull();

            Verification verification = concept.getVerification();

            assertThat(verification).isInstanceOf(AggregationVerification.class);

            AggregationVerification aggregationVerification = AggregationVerification.class.cast(verification);

            assertThat(aggregationVerification.getMax()).isEqualTo(20);
            assertThat(aggregationVerification.getMin()).isEqualTo(10);
            assertThat(aggregationVerification.getColumn()).isEqualTo("Throwables");
        }

        @Test
        void oneConceptVerificationRowCount() throws Exception {
            RuleSet ruleSet = readRuleSet("/yaml/concept-single-with-verification-rowcount.yaml");

            Concept concept = ruleSet.getConceptBucket().getAll().iterator().next();

            Verification verification = concept.getVerification();

            assertThat(verification).isInstanceOf(RowCountVerification.class);

            RowCountVerification rowCountVerification = RowCountVerification.class.cast(verification);

            assertThat(rowCountVerification.getMax()).isEqualTo(20);
            assertThat(rowCountVerification.getMin()).isEqualTo(10);
        }


        @Test
        void oneConceptWithMissingKeywordId() {
            assertThatThrownBy(() -> readRuleSet("/yaml/concept-single-with-missing-keyword-id.yaml"))
                .hasNoCause()
                .isExactlyInstanceOf(RuleException.class)
                .hasMessageMatching("^Rule source '[^']+' misses the keyword 'id' " +
                                    "at '\\$.concepts\\[0]'");
        }

        @Test
        void oneConceptComplex() throws Exception {
            RuleSet ruleSet = readRuleSet("/yaml/concept-single-simple.yaml");

            Concept concept = ruleSet.getConceptBucket().getAll().iterator().next();

            assertThat(concept.getId()).isEqualTo("java:Throwable");
            assertThat(concept.getDeprecation()).isNull();
            assertThat(concept.getDescription()).isEqualTo("Labels types deriving from java.lang.Throwable as \"Throwable\".");
            assertThat(concept.getParameters()).isEmpty();
            assertThat(concept.getRequiresConcepts()).isEmpty();
            assertThat(concept.getSeverity()).isEqualTo(Severity.MINOR);
            assertThat(concept.getReport()).isNotNull();
            assertThat(concept.getExecutable()).isNotNull().isInstanceOf(CypherExecutable.class);

            CypherExecutable executable = (CypherExecutable) concept.getExecutable();

            assertThat(executable.getLanguage()).isEqualTo("cypher");
            assertThat(executable.getSource()).isEqualTo("match\n" +
                                                             "  (throwable)-[:EXTENDS*]->(t:Type)\n" +
                                                             "where\n" +
                                                             "  t.fqn = 'java.lang.Throwable'\n" +
                                                             "SET\n" +
                                                             "  throwable:Throwable\n" +
                                                             "return\n" +
                                                             "  count(throwable) AS Throwables\n");
        }



        @Disabled
        @ParameterizedTest
        @CsvSource(value = {"/yaml/concept-with-parameter-invalid-missing-field-name.yaml|name",
                            "/yaml/concept-with-parameter-invalid-missing-field-type.yaml|type"},
                   delimiter = '|')
        void oneConceptParameterInvalidRequiredFieldIsMissing(String resourcePath, String missingKeyword) {
            assertThatThrownBy(() -> readRuleSet(resourcePath))
                .isExactlyInstanceOf(RuleException.class)
                .hasNoCause()
                .hasMessageMatching("^The concept '[^']+' " +
                                        "in rule source '[^']+' " + "" +
                                        "has an invalid parameter\\. The following keys are missing: "+
                                        missingKeyword);
        }

        @Disabled
        @CsvSource(value = {"/yaml/concept-with-parameter-and-unsupported-keyword.yml|unsupported"},
                   delimiter = '|')
        @ParameterizedTest
        void oneConceptParameterInvalidParameterUnsupportedKeyword(String resourcePath,
                                                                   String unsupportedKeyword) {
            assertThatThrownBy(() -> readRuleSet(resourcePath))
                .isExactlyInstanceOf(RuleException.class)
                .hasNoCause()
                .hasMessageMatching("^The concept '[^']+' " +
                                        "in rule source '[^']+' " +
                                        "has an invalid parameter\\. The following keys are not supported: "+
                                        unsupportedKeyword);
        }

        @Test
        void oneConceptParameterInvalidUnsupportedDatastructure() throws RuleException {
            String regex = "Rule source '[^']+' must have one of " +
                           "'boolean, byte, char, double, float, int, long, short, String' " +
                           "at '\\$.concepts\\[0].requiresParameters\\[0].type'";

            assertThatThrownBy(() -> readRuleSet("/yaml/concept-with-parameter-with-illegal-datastructure.yml"))
                .hasNoCause()
                .isExactlyInstanceOf(RuleException.class)
                .hasMessageMatching(regex);
        }

        @Test
        void oneConceptParameterInvalidMissingParameterName() {
            String regex = "Rule source '[^']+' contains at " +
                           "'\\$.concepts\\[0].requiresParameters\\[0].name' " +
                           "nothing where a scalar is expected";
            assertThatThrownBy(() -> readRuleSet("/yaml/concept-with-parameter-missing-name.yml"))
                .isExactlyInstanceOf(RuleException.class)
                .hasNoCause()
                .hasMessageMatching(regex);
        }

        @Test
        void oneConceptParamterInvalidMissingType() {
            String regex = "Rule source '[^']+' must have one of " +
                           "'boolean, byte, char, double, float, int, long, short, String'" +
                           " at '\\$.concepts\\[0].requiresParameters\\[0].type'";

            assertThatThrownBy(() -> readRuleSet("/yaml/concept-with-parameter-missing-type.yml"))
                .isExactlyInstanceOf(RuleException.class)
                .hasNoCause()
                .hasMessageMatching(regex);
        }

        @Test
        void oneConceptParameterInvalidUnsupportedType() {
            String regex = "Rule source '[^']+' must have one of " +
                           "'boolean, byte, char, double, float, int, long, short, String' " +
                           "at '\\$.concepts\\[0].requiresParameters\\[0].type'";

            assertThatThrownBy(() -> readRuleSet("/yaml/concept-with-parameter-unsupported-type.yml"))
                .isExactlyInstanceOf(RuleException.class)
                .hasNoCause()
                .hasMessageMatching(regex);
        }

        @Test
        void oneConceptParameterInvalidMissingDefaultValue() {
            String regex = "Rule source " +
                           "'[^']+' contains at '\\$.concepts\\[0].requiresParameters\\[0].defaultValue' " +
                           "nothing where a scalar is expected";

            assertThatThrownBy(() -> readRuleSet("/yaml/concept-with-parameter-missing-default-value.yml"))
                .isExactlyInstanceOf(RuleException.class)
                .hasNoCause()
                .hasMessageMatching(regex);
        }

        @Test
        void oneConceptOneDepedencyNotOptional() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/concept-single-with-dependency-one.yaml");
            Concept concept = ruleSet.getConceptBucket().getAll().iterator().next();

            assertThat(concept.getRequiresConcepts()).containsKey("concept:other");
            assertThat(concept.getRequiresConcepts().get("concept:other")).isFalse();
            assertThat(concept.getRequiresConcepts()).hasSize(1);
        }

        @Test
        void oneConceptOneDependencyWithUnsupportedKey() throws RuleException {
            String messageRegex = "Rule source '[^']+' contains the unknown " +
                                  "keyword 'thisKeyIsNotSupported' at " +
                                  "'\\$.concepts\\[0].requiresConcepts\\[0]'";

            assertThatThrownBy(() -> readRuleSet("/yaml/concept-single-with-one-dependency-and-unsupported-key.yaml"))
                .isExactlyInstanceOf(RuleException.class)
                .hasNoCause()
                .hasMessageMatching(messageRegex);
        }

        @Test
        void oneConceptWithRuleLanguageExplicitlySetToCypher() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/concept-single-rule-language-explicitly-set.yaml");

            assertThat(ruleSet.getConceptBucket().getById("java:Throwable")).isNotNull();

            Concept concept = ruleSet.getConceptBucket().getById("java:Throwable");

            assertThat(concept.getExecutable().getLanguage()).isEqualTo("cypher");
        }

        @Test
        void oneConceptWithRuleLanguageExplicitlySetToJavaScript() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/concept-single-rule-language-explicitly-set-to-js.yaml");

            assertThat(ruleSet.getConceptBucket().getById("java:Throwable")).isNotNull();

            Concept concept = ruleSet.getConceptBucket().getById("java:Throwable");

            assertThat(concept.getExecutable().getLanguage()).isEqualTo("JavaScript");
        }

        @Test
        void oneConceptWithRuleLanguageExplicitlySetToCypherImplicitly() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/concept-single-rule-language-not-explicitly-set.yaml");

            assertThat(ruleSet.getConceptBucket().getById("java:Throwable")).isNotNull();

            Concept concept = ruleSet.getConceptBucket().getById("java:Throwable");

            assertThat(concept.getExecutable().getLanguage()).isEqualTo("cypher");
        }

        @Test
        void oneConceptOneDepdencyWithMissingRequiredKey() throws RuleException {
            String messageRegex = "^Rule source '[^']+' misses the keyword 'refId' " +
                                  "at '\\$.concepts\\[0].requiresConcepts\\[0]'";
            assertThatThrownBy(() -> readRuleSet("/yaml/concept-single-with-dependency-with-missing-required-key.yaml"))
                .hasNoCause()
                .hasMessageMatching(messageRegex)
                .isExactlyInstanceOf(RuleException.class);
        }

        @Test
        void oneConceptProvidesConcepts() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/concept-provides-concepts.yaml");

            assertThat(ruleSet.getConceptBucket().getById("test:ProvidingConcept")).isNotNull();

            Concept concept = ruleSet.getConceptBucket().getById("test:ProvidingConcept");

            assertThat(concept.getProvidedConcepts()).containsExactlyInAnyOrder("test:Concept1", "test:Concept2");

        }
    }


    @Nested
    class DocumentRelated {
        @Test
        void documentEmpty() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/document-empty.yaml");

            assertThat(ruleSet.getGroupsBucket().getAll()).isEmpty();
            assertThat(ruleSet.getConceptBucket().getAll()).isEmpty();
            assertThat(ruleSet.getConstraintBucket().getAll()).isEmpty();
        }

        @Test
        void documentEmptyEmptyFile() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/document-empty-empty-file.yaml");

            assertThat(ruleSet.getGroupsBucket().getAll()).isEmpty();
            assertThat(ruleSet.getConceptBucket().getAll()).isEmpty();
            assertThat(ruleSet.getConstraintBucket().getAll()).isEmpty();
        }

        @Test
        void documentOnlyToplevelKeywordsButAllEmpty() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/document-only-toplevel-keywords-but-all-empty.yaml");

            assertThat(ruleSet.getGroupsBucket().getAll()).isEmpty();
            assertThat(ruleSet.getConceptBucket().getAll()).isEmpty();
            assertThat(ruleSet.getConstraintBucket().getAll()).isEmpty();
        }

        @Test
        void documentUnknownTopLevelKeyword() throws Exception {
            String regex = "Rule source '[^']+' contains the unknown " +
                           "keyword 'foobar' at '\\$'";

            assertThatThrownBy(() -> readRuleSet("/yaml/document-unknown-toplevel-keyword.yml"))
                .hasNoCause()
                .isExactlyInstanceOf(RuleException.class)
                .hasMessageMatching(regex);
        }
    }

    @Nested
    class RuleSourceRelated {
        @ParameterizedTest
        @MethodSource(value = {"com.buschmais.jqassistant.core.rule.impl.reader.YamlRuleParserPluginTest#windowsUrlPositive",
                               "com.buschmais.jqassistant.core.rule.impl.reader.YamlRuleParserPluginTest#unixUrlPositive",
                               "com.buschmais.jqassistant.core.rule.impl.reader.YamlRuleParserPluginTest#httpUrlPositive"})
        void acceptsValidRuleSources(URL url) throws Exception {
            RuleSource ruleSource = Mockito.mock(RuleSource.class);

            when(ruleSource.getURL()).thenReturn(url);

            YamlRuleParserPlugin plugin = new YamlRuleParserPlugin();

            assertThat(plugin.accepts(ruleSource)).isTrue();
        }

        @ParameterizedTest
        @MethodSource(value = {"com.buschmais.jqassistant.core.rule.impl.reader.YamlRuleParserPluginTest#windowsUrlNegative",
                               "com.buschmais.jqassistant.core.rule.impl.reader.YamlRuleParserPluginTest#unixUrlNegative",
                               "com.buschmais.jqassistant.core.rule.impl.reader.YamlRuleParserPluginTest#httpUrlNegative"})
        void doesNotAcceptInvalidRuleSources(URL url) throws Exception {
            RuleSource ruleSource = Mockito.mock(RuleSource.class);

            when(ruleSource.getURL()).thenReturn(url);

            YamlRuleParserPlugin plugin = new YamlRuleParserPlugin();

            assertThat(plugin.accepts(ruleSource)).isFalse();
        }

    }

    @Nested
    class GroupRelated {
        @Test
        void oneGroupOneIncludedConstraint() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/group-single-one-included-constraint.yaml");

            GroupsBucket groups = ruleSet.getGroupsBucket();

            assertThat(groups.size()).isEqualTo(1);

            Group group = ruleSet.getGroupsBucket().getAll().iterator().next();
            Map<String, Severity> constraints = group.getConstraints();

            assertThat(constraints).hasSize(1);
            assertThat(constraints).containsKey("uuu");
            assertThat(constraints.get("uuu")).isNull();

            assertThat(group.getGroups()).isEmpty();
            assertThat(group.getConcepts()).isEmpty();
        }

        @Test
        void oneGroupIncludeConceptMissingRefId() throws Exception {
            String regex = "Rule source '[^']+' misses the keyword 'refId' " +
                           "at '\\$.groups\\[0].includedConcepts\\[0]'";

            assertThatThrownBy(() ->readRuleSet("/yaml/group-single-include-concept-missing-refid.yaml"))
                .hasNoCause()
                .isExactlyInstanceOf(RuleException.class)
                .hasMessageMatching(regex);
        }

        @Test
        void oneGroupSeverityExplicitlySet() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/group-single-severity-explicitly-set.yaml");

            assertThat(ruleSet.getGroupsBucket().size()).isEqualTo(1);

            Group group = ruleSet.getGroupsBucket().getById("a");

            assertThat(group.getSeverity()).isEqualTo(Severity.BLOCKER);
        }

        @Test
        void oneGroupSeverityNotSet() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/group-single-severity-not-set.yaml");

            assertThat(ruleSet.getGroupsBucket().size()).isEqualTo(1);

            Group group = ruleSet.getGroupsBucket().getById("abc");

            assertThat(group.getSeverity()).isNull();
        }

        @Test
        void oneGroupUnknownKeyword() throws RuleException {
            String regex = "Rule source '[^']+' contains the unknown keyword " +
                           "'foo' at '\\$.groups\\[0]'";

            assertThatThrownBy(() -> readRuleSet("/yaml/group-single-with-unknown-keyword.yaml"))
                .isInstanceOf(RuleException.class)
                .hasNoCause()
                .hasMessageMatching(regex);
        }

        @Test
        void oneGroupIncludeConstraintWithDifferentSeverity() throws Exception {
            RuleSet ruleSet = readRuleSet("/yaml/group-single-include-constraint-different-severity.yaml");

            Group group = ruleSet.getGroupsBucket().getAll().iterator().next();

            Map<String, Severity> constraints = group.getConstraints();

            assertThat(constraints).hasSize(1);
            assertThat(constraints).containsKey("referenced_constraint");
            assertThat(constraints.get("referenced_constraint")).isEqualTo(Severity.INFO);
            assertThat(constraints.get("referenced_constraint")).isNotEqualByComparingTo(Constraint.DEFAULT_SEVERITY);
        }


        @Test
        void groupWithAnIncludedWithoutValueForSeverity() {
            String regex = "Rule source '[^']+' must have one of " +
                           "'blocker, critical, major, minor, info' at " +
                           "'\\$.groups\\[0].includedConcepts\\[0].severity'";

            assertThatThrownBy(() -> readRuleSet("/yaml/group-single-with-include-concept-no-value-for-severity.yaml"))
                .isInstanceOf(RuleException.class)
                .hasNoCause()
                .hasMessageMatching(regex);
        }

        @Test
        void oneGroupIncludeConceptSeverityUnknown() throws Exception {
            String regex = "Rule source '[^']+' must have one of " +
                           "'blocker, critical, major, minor, info' at " +
                           "'\\$.groups\\[0].includedConcepts\\[0].severity'";

            assertThatThrownBy(() -> readRuleSet("/yaml/group-single-include-concept-severity-unknown.yml"))
                .hasNoCause()
                .isExactlyInstanceOf(RuleException.class)
                .hasMessageMatching(regex);
        }

        @Test
        void oneGroup() throws Exception {
            RuleSet ruleSet = readRuleSet("/yaml/group-single.yaml");

            assertThat(ruleSet.getGroupsBucket().size()).isEqualTo(1);
            assertThat(ruleSet.getConstraintBucket().size()).isEqualTo(0);
            assertThat(ruleSet.getConceptBucket().size()).isEqualTo(0);
        }

        @Test
        void oneGroupIncludeConstraintEmptyList() throws Exception {
            RuleSet ruleSet = readRuleSet("/yaml/group-single-include-constraint-empty-list.yaml");

            Group group = ruleSet.getGroupsBucket().getAll().iterator().next();

            assertThat(group.getConstraints()).isEmpty();
            assertThat(group.getConcepts()).hasSize(1);
        }

        @Test
        void oneGroupIncludeConceptEmptyList() throws Exception {
            RuleSet ruleSet = readRuleSet("/yaml/group-single-include-concept-empty-list.yaml");

            Group group = ruleSet.getGroupsBucket().getAll().iterator().next();

            assertThat(group.getConcepts()).isEmpty();
            assertThat(group.getConstraints()).hasSize(1);
        }

        @Test
        void oneGroupMissingKeyword() throws Exception {
            assertThatThrownBy(() -> readRuleSet("/yaml/group-single-missing-keyword.yaml"))
                .hasNoCause()
                .isExactlyInstanceOf(RuleException.class)
                .hasMessageMatching("Rule source '[^']+' misses the keyword 'id' at '\\$.groups\\[0]'");
        }

        @Test
        void oneGroupIncludeGroupEmptyList() throws Exception {
            RuleSet ruleSet = readRuleSet("/yaml/group-single-include-group-empty-list.yaml");

            Group group = ruleSet.getGroupsBucket().getAll().iterator().next();

            assertThat(group.getGroups()).isEmpty();
            assertThat(group.getConcepts()).hasSize(1);
            assertThat(group.getConstraints()).hasSize(1);

        }

        @Test
        void twoGroups() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/group-two.yaml");

            assertThat(ruleSet.getGroupsBucket().size()).isEqualTo(2);
            assertThat(ruleSet.getConstraintBucket().size()).isEqualTo(0);
            assertThat(ruleSet.getConceptBucket().size()).isEqualTo(0);
        }

        @Test
        void oneGroupIncludeConceptComplex() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/group-single-with-one-included-concept.yaml");

            GroupsBucket groups = ruleSet.getGroupsBucket();

            assertThat(groups.size()).isEqualTo(1);

            Group group = groups.getAll().iterator().next();
            Map<String, Severity> concepts = group.getConcepts();

            assertThat(concepts).hasSize(1);
            assertThat(concepts).containsKey("efg");
            assertThat(concepts.get("efg")).isNull();

            assertThat(group.getConstraints()).isEmpty();
            assertThat(group.getGroups()).isEmpty();
        }

        @Test
        void oneGroupIncludeConceptExplicitSeveritySpecification() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/group-single-with-one-included-concept-with-own-severity.yaml");

            GroupsBucket groups = ruleSet.getGroupsBucket();

            assertThat(groups.size()).isEqualTo(1);

            Group group = groups.getAll().iterator().next();
            Map<String, Severity> concepts = group.getConcepts();

            assertThat(concepts).hasSize(1);
            assertThat(concepts).containsKey("xxx");
            assertThat(concepts.get("xxx")).isEqualTo(Severity.MINOR);

            assertThat(group.getConstraints()).isEmpty();
            assertThat(group.getGroups()).isEmpty();
        }


        @Test
        void oneGroupIncludeOneGroup() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/group-single-with-one-included-group.yaml");

            GroupsBucket groups = ruleSet.getGroupsBucket();

            assertThat(groups.size()).isEqualTo(1);

            Group group = ruleSet.getGroupsBucket().getAll().iterator().next();

            Map<String, Severity> includedGroups = group.getGroups();

            assertThat(includedGroups).containsKey("mmm");
            assertThat(includedGroups.get("mmm")).isEqualTo(Severity.BLOCKER);

            assertThat(includedGroups).hasSize(1);
        }

        @Test
        void oneGroupIncludeConceptAdditionalKeyword() throws RuleException {
            assertThatThrownBy(() -> readRuleSet("/yaml/group-single-include-concept-additional-keyword.yaml"))
                .hasNoCause()
                .isExactlyInstanceOf(RuleException.class)
                .hasMessageMatching("Rule source '[^']+' contains the unknown keyword 'foo' at '\\$.groups\\[0\\].includedConcepts\\[0\\]'");
        }

        @Test
        void oneGroupIncludeConceptTwo() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/group-single-include-concept-two.yaml");

            GroupsBucket groups = ruleSet.getGroupsBucket();

            assertThat(groups.size()).isEqualTo(1);

            Group group = groups.getAll().iterator().next();

            Map<String, Severity> includedConcepts = group.getConcepts();

            assertThat(includedConcepts).containsOnlyKeys("xxx", "yyy");
            assertThat(includedConcepts.get("xxx")).isNull();
            assertThat(includedConcepts.get("yyy")).isNull();
        }

        @Test
        void oneGroupIncludeConstraintTwo() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/group-single-include-constraint-two.yaml");

            GroupsBucket groups = ruleSet.getGroupsBucket();

            assertThat(groups.size()).isEqualTo(1);

            Group group = groups.getAll().iterator().next();

            assertThat(group.getConstraints()).containsOnlyKeys("x", "y");
            assertThat(group.getConcepts()).isEmpty();
            assertThat(group.getGroups()).isEmpty();
        }

        @Test
        void singleGroupWithTwoIncludedGroups() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/group-single-with-two-included-groups.yaml");

            GroupsBucket groups = ruleSet.getGroupsBucket();

            assertThat(groups.getAll()).hasSize(1);
            assertThat(groups.getById("p_g")).isNotNull();

            Group group = groups.getById("p_g");

            assertThat(group.getGroups().containsKey("a_g")).isNotNull();
            assertThat(group.getGroups().containsKey("b_g")).isNotNull();
            assertThat(group.getGroups()).hasSize(2);
        }
    }

    @Nested
    class I18N {
        @Test
        void japaneseDescription() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/i18n-japanese-description.yaml");

            Concept concept = ruleSet.getConceptBucket().getAll().iterator().next();

            assertThat(concept.getDescription()).isEqualTo("おはようございます。");
        }

        @Test
        void emojiUsage() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/i18n-emoji-usage.yaml");

            Concept concept = ruleSet.getConceptBucket().getAll().iterator().next();

            assertThat(concept.getId()).isEqualTo("\uD83D\uDC36");
            assertThat(concept.getDescription()).isEqualTo("\uD83D\uDC31");
        }
    }

    @Nested
    class VerificationRelated {
        @Test
        void unsupportedKeyword() {
            String messageRegex = "^Rule source '[^']+' contains the unknown " +
                                  "keyword 'unsupportedKeyWord' at " +
                                  "'\\$.concepts\\[0].verify'";

            assertThatThrownBy(() -> readRuleSet("/yaml/concept-single-with-verification-unsupported-keyword.yaml"))
                .hasNoCause()
                .hasMessageMatching(messageRegex)
                .isExactlyInstanceOf(RuleException.class);
        }

        @Test
        void aggregationAndRowcount() {
            String messageRegex = "Rule source '[^']+' can have only one of " +
                                  "the given keywords at '\\$.concepts\\[0].verify'";

            assertThatThrownBy(() -> readRuleSet("/yaml/concept-single-with-verification-and-rowcount.yaml"))
                .hasNoCause()
                .isExactlyInstanceOf(RuleException.class)
                .hasMessageMatching(messageRegex);
        }
    }


    @Nested
    class ReportRelated {
        @Test
        void onlyReportKeywordIsGiven() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/constraint-with-report-without-any-key.yaml");

            assertThat(ruleSet.getConstraintBucket().getAll()).hasSize(1);

            Constraint constraint = ruleSet.getConstraintBucket().getAll().iterator().next();

            assertThat(constraint.getReport()).isNotNull();

            Report report = constraint.getReport();

            assertThat(report.getProperties()).isEmpty();
            assertThat(report.getPrimaryColumn()).isNull();
            assertThat(report.getSelectedTypes()).isNullOrEmpty();
        }

        @Test
        void reportBlockWithNoProperties() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/constraint-with-report-no-properties.yaml");

            Constraint constraint = ruleSet.getConstraintBucket().getAll().iterator().next();

            assertThat(constraint.getReport()).isNotNull();

            Report report = constraint.getReport();

            assertThat(report.getProperties()).isEmpty();
        }

        @Test
        void reportBlockWithManyProperties() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/constraint-with-report-many-properties.yaml");

            Constraint constraint = ruleSet.getConstraintBucket().getAll().iterator().next();

            assertThat(constraint.getReport()).isNotNull();

            Report report = constraint.getReport();

            assertThat(report.getProperties()).isNotNull();
            assertThat(report.getProperties()).hasSize(4);
            assertThat(report.getProperties()).contains(makeEntry("asciidoc.foobar", "A"), makeEntry("b", "B"),
                                                        makeEntry("c", "C"), makeEntry("A", 3));
        }

        @Test
        void reportBlockWithPrimaryColumn() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/constraint-with-report-no-properties.yaml");

            Constraint constraint = ruleSet.getConstraintBucket().getAll().iterator().next();

            assertThat(constraint.getReport()).isNotNull();

            Report report = constraint.getReport();

            assertThat(report.getPrimaryColumn()).isEqualTo("c");
        }

        @Test
        void reportBlockWithReportType() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/constraint-with-report-with-report-type.yaml");

            Constraint constraint = ruleSet.getConstraintBucket().getAll().iterator().next();

            assertThat(constraint.getReport()).isNotNull();

            Report report = constraint.getReport();

            assertThat(report.getSelectedTypes()).hasSize(1);
            assertThat(report.getSelectedTypes()).contains("csv");
        }

        @Test
        void reportBlockWithReportTypeMultipleCommaSeparatedValues() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/constraint-with-report-with-report-type-multiple.yaml");

            Constraint constraint = ruleSet.getConstraintBucket().getAll().iterator().next();

            assertThat(constraint.getReport()).isNotNull();

            Report report = constraint.getReport();

            assertThat(report.getSelectedTypes()).contains("csv", "json", "xml");
            assertThat(report.getSelectedTypes()).hasSize(3);
        }

        @Test
        void reportSectionWithInvalidBlockStructureNotNeededMap() {
            String regex = "^Rule source '[^']+' contains " +
                           "at '\\$.constraints\\[0].report.primaryColumn' a map " +
                           "where a scalar is expected";

            assertThatThrownBy(() -> readRuleSet("/yaml/constraint-with-report-with-invalid-block-structure-02.yaml"))
                .hasNoCause()
                .isInstanceOf(RuleException.class)
                .hasMessageMatching(regex);
        }

        @Test
        void reportSectionWithInvalidBlockStructureSequence() {
            String regex = "^Rule source '[^']+' contains " +
                           "at '\\$.constraints\\[0].report' a sequence where a " +
                           "map is expected";

            assertThatThrownBy(() -> readRuleSet("/yaml/constraint-with-report-with-invalid-block-structure-01.yaml"))
                .hasNoCause()
                .isInstanceOf(RuleException.class)
                .hasMessageMatching(regex);
        }

        @Test
        void reportSectionWithAllPossibleKeys() throws RuleException {
            RuleSet ruleSet = readRuleSet("/yaml/constraint-with-report-all-possible-keys.yaml");

            Constraint constraint = ruleSet.getConstraintBucket().getAll().iterator().next();

            assertThat(constraint.getReport()).isNotNull();

            Report report = constraint.getReport();

            assertThat(report.getSelectedTypes()).hasSize(1);
            assertThat(report.getSelectedTypes()).contains("snafu");
            assertThat(report.getPrimaryColumn()).isEqualTo("lamp");
            assertThat(report.getProperties()).hasSize(3);
        }

        @Test
        void reportSectionWithUnknownKeyword() throws RuleException {
            assertThatThrownBy(() -> readRuleSet("/yaml/constraint-with-report-and-with-unknown-keyword.yml"))
                .hasNoCause()
                .isInstanceOf(RuleException.class)
                .hasMessageMatching("^Rule source '[^']+' contains the unknown keyword 'wtf' at '\\$.constraints\\[0].report'");
        }
    }

    RuleSet readRuleSet(String resource) throws RuleException {
        return RuleSetTestHelper.readRuleSet(resource, rule);
    }

    static Stream<String> windowsUrlPositive() {
        return Stream.of("file://laptop/My%20Documents/FileSchemeURIs.yml",
                         "file://laptop/My%20Documents/FileSchemeURIs.YML",
                         "file:///C:/Documents%20and%20Settings/davris/FileSchemeURIs.yaml");
    }

    static Stream<String> unixUrlPositive() {
        return Stream.of("file:///usr/foo/bar/rules.yaml",
                         "file:///usr/foo/bar/rules.YAML",
                         "file:///usr/foo/bar/rules.yml");
    }

    static Stream<String> httpUrlPositive() {
        return Stream.of("https://host.domain/rules.yaml",
                         "https://host.domain/rules.yml",
                         "https://host.domain/rules.YML");
    }

    static Stream<String> windowsUrlNegative() {
        return Stream.of("file://laptop/My%20Documents/FileSchemeURIs.adoc",
                         "file://laptop/My%20Documents/FileSchemeURIs.asciidoctor",
                         "file:///C:/Documents%20and%20Settings/davris/FileSchemeURIs.XML");
    }

    static Stream<String> unixUrlNegative() {
        return Stream.of("file:///usr/foo/bar/rules.asciidoctor",
                         "file:///usr/foo/bar/rules.adoc",
                         "file:///usr/foo/bar/rules.doc");
    }

    static Stream<String> httpUrlNegative() {
        return Stream.of("https://host.domain/rules.xMl",
                         "https://host.domain/rules.adoc",
                         "https://host.domain/rules.ADOC");
    }

    static private Map.Entry<String, Object> makeEntry(String key, Object value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

}
