package com.buschmais.jqassistant.core.rule.api.model;

import java.io.File;
import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.impl.SourceExecutable;
import com.buschmais.jqassistant.core.shared.asciidoc.AsciidoctorFactory;

import net.sourceforge.plantuml.png.MetadataTag;
import org.asciidoctor.ast.StructuralNode;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class ExecutablesTest {

    @Mock
    private Rule rule;

    @Test
    void xml() throws Exception {
        verifyRuleset(RuleSetTestHelper.readRuleSet("/executables.xml", rule));
    }

    @Test
    void asciidoc() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/executables.adoc", rule);
        verifyRuleset(ruleSet);
        verifyConceptExecutable(ruleSet, "test:Table", SourceExecutable.class, StructuralNode.class, "table");
    }

    @Test
    void plantuml() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/executables.adoc", rule);
        Concept concept = verifyConceptExecutable(ruleSet, "test:PlantUML", SourceExecutable.class, StructuralNode.class, "plantuml");
        StructuralNode abstractBlock = (StructuralNode) concept.getExecutable().getSource();
        String imagesDirectoryAttribute = (String) abstractBlock.getDocument().getAttributes().get(AsciidoctorFactory.ATTRIBUTE_IMAGES_OUT_DIR);
        assertThat(imagesDirectoryAttribute, notNullValue());
        File imagesOutDir = new File(imagesDirectoryAttribute);
        assertThat(imagesOutDir.exists(), equalTo(true));
        String fileName = (String) abstractBlock.getAttributes().get("target");
        assertThat(fileName, notNullValue());
        File diagramFile = new File(imagesOutDir, fileName);
        assertThat("Expected file " + diagramFile + " does not exist.", diagramFile.exists(), equalTo(true));
        String diagramMetadata = new MetadataTag(diagramFile, "plantuml").getData();
        assertThat(diagramMetadata, containsString("@startuml"));
        assertThat(diagramMetadata, containsString("@enduml"));
    }

    private void verifyRuleset(RuleSet ruleSet) throws RuleException {
        verifyConceptExecutable(ruleSet, "test:CypherConcept", CypherExecutable.class, String.class, "cypher");
        verifyConceptExecutable(ruleSet, "test:ScriptConcept", ScriptExecutable.class, String.class, "javascript");
        verifyConceptExecutable(ruleSet, "test:SourceConcept", SourceExecutable.class, String.class, "cypher");
        verifyConceptExecutable(ruleSet, "test:SourceConceptUpperCase", SourceExecutable.class, String.class, "cypher");
        verifyConstraintExecutable(ruleSet, "test:CypherConstraint", CypherExecutable.class);
        verifyConstraintExecutable(ruleSet, "test:ScriptConstraint", ScriptExecutable.class);
        verifyConstraintExecutable(ruleSet, "test:SourceConstraint", SourceExecutable.class);
        verifyConstraintExecutable(ruleSet, "test:SourceConstraintUpperCase", SourceExecutable.class);
    }

    private Concept verifyConceptExecutable(RuleSet ruleSet, String id, Class<? extends Executable> type, Class<?> expectedSourceType, String expectedLanguage)
        throws RuleException {
        Concept concept = ruleSet.getConceptBucket().getById(id);
        assertThat(concept, notNullValue());
        Executable<?> executable = concept.getExecutable();
        assertThat(concept.getId(), executable, CoreMatchers.<Executable>instanceOf(type));
        assertThat(concept.getId(), executable.getSource(), instanceOf(expectedSourceType));
        assertThat(concept.getId(), executable.getLanguage(), equalTo(expectedLanguage));
        Map<String, Boolean> requiresConcepts = concept.getRequiresConcepts();
        assertThat(requiresConcepts, notNullValue());
        assertThat(requiresConcepts.containsKey("test:RequiredConcept"), equalTo(true));
        return concept;
    }

    private Constraint verifyConstraintExecutable(RuleSet ruleSet, String id, Class<? extends Executable> type) throws RuleException {
        Constraint constraint = ruleSet.getConstraintBucket().getById(id);
        assertThat(constraint, notNullValue());
        assertThat(constraint.getExecutable(), CoreMatchers.<Executable>instanceOf(type));
        Map<String, Boolean> requiresConcepts = constraint.getRequiresConcepts();
        assertThat(requiresConcepts, notNullValue());
        assertThat(requiresConcepts.containsKey("test:RequiredConcept"), equalTo(true));
        return constraint;
    }

}
