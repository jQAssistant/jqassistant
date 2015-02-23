package com.buschmais.jqassistant.core.analysis.impl;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.ContentPart;
import org.asciidoctor.ast.StructuredDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;

/**
 * @author mh
 * @since 12.10.14
 */
public class AsciiDocRuleSetReader implements RuleSetReader {

    private static final Set<String> RULETYPES = new HashSet<>(asList("concept", "constraint"));

    private static final Logger LOGGER = LoggerFactory.getLogger(AsciiDocRuleSetReader.class);

    /**
     *
     */
    private Asciidoctor cachedAsciidoctor;

    @Override
    public RuleSet read(List<? extends RuleSource> sources) {
        Map<String, Template> queryTemplates = Collections.emptyMap();
        Map<String, MetricGroup> metricGroups = Collections.emptyMap();
        Map<String, Group> groups = Collections.emptyMap();

        Map<String, Concept> concepts = new LinkedHashMap<>();
        Map<String, Constraint> constraints = new LinkedHashMap<>();

        for (RuleSource source : sources) {
            if (source.isType(RuleSource.Type.AsciiDoc)) {
                readDocument(source, concepts, constraints);
            }
        }
        return new DefaultRuleSet(queryTemplates, concepts, constraints, groups, metricGroups);
    }

    public void readDocument(RuleSource source, Map<String, Concept> concepts, Map<String, Constraint> constraints) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Asciidoctor.STRUCTURE_MAX_LEVEL, 10);
        InputStream stream;
        try {
            stream = source.getInputStream();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read rules from '" + source.getId() + "'.", e);
        }
        StructuredDocument doc = getAsciidoctor().readDocumentStructure(new InputStreamReader(stream), parameters);
        extractRules(doc, concepts, constraints);
    }

    /**
     * Return an ascii doctor instance.
     * <p>
     * Initialization is quite expensive, therefore doing it lazy.
     * </p>
     * 
     * @return The ascii doctor instance.
     */
    private Asciidoctor getAsciidoctor() {
        if (cachedAsciidoctor == null) {
            LOGGER.debug("Creating Asciidoctor instance.");
            cachedAsciidoctor = Asciidoctor.Factory.create();
        }
        return cachedAsciidoctor;
    }

    /**
     * Extract the rules from the given document.
     * 
     * @param doc
     *            The document.
     * @param concepts
     *            The map of concepts to fill.
     * @param constraints
     *            The map of constraints to fill.
     */
    private void extractRules(StructuredDocument doc, Map<String, Concept> concepts, Map<String, Constraint> constraints) {
        for (ContentPart part : findListings(doc)) {
            Map<String, Object> attributes = part.getAttributes();
            String id = part.getId();
            String description = attributes.get("title").toString();
            Set<String> requiresConcepts = getDependencies(attributes);
            String cypher = null;
            Script script = null;
            Object language = part.getAttributes().get("language");
            String source = unescapeHtml(part.getContent());
            if ("cypher".equals(language)) {
                cypher = source;
            } else {
                script = new Script(language.toString(), source);
            }
            if ("concept".equals(part.getRole())) {
                Severity severity = getSeverity(part, Concept.DEFAULT_SEVERITY);
                Concept concept = new Concept(id, description, severity, null, cypher, script, null, Collections.<String, Object> emptyMap(), requiresConcepts);
                concepts.put(concept.getId(), concept);
            } else if ("constraint".equals(part.getRole())) {
                Severity severity = getSeverity(part, Constraint.DEFAULT_SEVERITY);
                Constraint concept = new Constraint(id, description, severity, null, cypher, script, null, Collections.<String, Object> emptyMap(),
                        requiresConcepts);
                constraints.put(concept.getId(), concept);
            }
        }
    }

    /**
     * Extract the optional severity of a rule.
     * 
     * @param part
     *            The part representing a rule.
     * @param defaultSeverity
     *            The default severity to use if no severity is specified.
     * @return The severity.
     */
    private Severity getSeverity(ContentPart part, Severity defaultSeverity) {
        Object severity = part.getAttributes().get("severity");
        return severity == null ? defaultSeverity : Severity.fromValue(severity.toString().toLowerCase());
    }

    /**
     * Unescapes the content of a rule.
     *
     * TODO do better, or even better add a part.get(Original|Raw)Content() to
     * asciidoctor
     * 
     * @param content
     *            The content of a rule.
     * @return The unescaped rule
     */
    private String unescapeHtml(String content) {
        return content.replace("&lt;", "<").replace("&gt;", ">");
    }

    /**
     * Get the dependencies declared for a rule.
     * 
     * @param attributes
     *            The attributes of the rule.
     * @return The set of dependencies.
     */
    private Set<String> getDependencies(Map<String, Object> attributes) {
        String depends = (String) attributes.get("depends");
        Set<String> dependencies = new HashSet<>();
        if (depends != null && !depends.trim().isEmpty()) {
            dependencies.addAll(asList(depends.split("\\s*,\\s*")));
        }
        return dependencies;
    }

    /**
     * Find all content parts representing source code listings with a role that
     * represents a rule.
     * 
     * @param doc
     *            The document.
     * @return A collection of content parts representing rules.
     */
    private static Collection<ContentPart> findListings(StructuredDocument doc) {
        Set<ContentPart> result = new LinkedHashSet<ContentPart>();
        result.addAll(findListings(doc.getParts()));
        return result;
    }

    /**
     * Find all content parts representing source code listings with a role that
     * represents a rule.
     * 
     * @param parts
     *            The content parts of the document.
     * @return A collection of content parts representing rules.
     */
    private static Collection<ContentPart> findListings(Collection<ContentPart> parts) {
        Set<ContentPart> result = new LinkedHashSet<ContentPart>();
        if (parts != null) {
            for (ContentPart part : parts) {
                if ("listing".equals(part.getContext()) && "source".equals(part.getStyle()) && RULETYPES.contains(part.getRole())) {
                    result.add(part);
                }
                result.addAll(findListings(part.getParts()));
            }
        }
        return result;
    }
}
