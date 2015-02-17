package com.buschmais.jqassistant.core.analysis.impl;

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

    private void extractRules(StructuredDocument doc, Map<String, Concept> concepts, Map<String, Constraint> constraints) {
        for (ContentPart part : findListings(doc)) {
            Map<String, Object> attributes = part.getAttributes();
            String id = part.getId();
            String description = attributes.get("title").toString();
            String cypher = unescapeHtml(part.getContent());
            Set<String> requiresConcepts = getDependencies(attributes);
            if ("concept".equals(part.getRole())) {
                Concept concept = new Concept(id, description, Severity.INFO, null, cypher, null, null, Collections.<String, Object> emptyMap(),
                        requiresConcepts);
                concepts.put(concept.getId(), concept);
            } else if ("constraint".equals(part.getRole())) {
                Constraint concept = new Constraint(id, description, Severity.INFO, null, cypher, null, null, Collections.<String, Object> emptyMap(),
                        requiresConcepts);
                constraints.put(concept.getId(), concept);
            }
        }
    }

    // todo do better, or even better add a part.get(Original|Raw)Content() to
    // asciidoctor
    private String unescapeHtml(String content) {
        return content.replace("&lt;", "<").replace("&gt;", ">");
    }

    private Set<String> getDependencies(Map<String, Object> attributes) {
        String depends = (String) attributes.get("depends");
        Set<String> dependencies = new HashSet<>();
        if (depends != null && !depends.trim().isEmpty()) {
            dependencies.addAll(Arrays.asList(depends.split("\\s*,\\s*")));
        }
        return dependencies;
    }

    private static Collection<ContentPart> findListings(StructuredDocument doc) {
        Set<ContentPart> result = new LinkedHashSet<ContentPart>();
        result.addAll(findListings(doc.getParts()));
        return result;
    }

    private static Collection<ContentPart> findListings(Collection<ContentPart> parts) {
        Set<ContentPart> result = new LinkedHashSet<ContentPart>();
        if (parts != null) {
            for (ContentPart part : parts) {
                if ("listing".equals(part.getContext()) && "source".equals(part.getStyle()) && "cypher".equals(part.getAttributes().get("language"))) {
                    result.add(part);
                }
                result.addAll(findListings(part.getParts()));
            }
        }
        return result;
    }
}
