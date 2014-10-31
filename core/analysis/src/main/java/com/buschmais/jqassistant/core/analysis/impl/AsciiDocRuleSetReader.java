package com.buschmais.jqassistant.core.analysis.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.ContentPart;
import org.asciidoctor.ast.DocumentHeader;
import org.asciidoctor.ast.StructuredDocument;
import org.asciidoctor.ast.Title;
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

    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    @Override
    public RuleSet read(List<? extends RuleSource> sources) {
        Map<String, MetricGroup> metricGroups = Collections.emptyMap();
        Map<String, Group> groups = Collections.emptyMap();

        Map<String, Concept> concepts = new LinkedHashMap<>();
        Map<String, Constraint> constraints = new LinkedHashMap<>();
        Map<String, String[]> dependencies = new HashMap<>();

        for (RuleSource source : sources) {
            if (source.isType(RuleSource.Type.AsciiDoc)) {
                readDocument(source, concepts, constraints, dependencies);
            }
        }
        resolveDependencies(dependencies, concepts, concepts);
        resolveDependencies(dependencies, constraints, concepts);

        return new RuleSet(concepts, constraints, groups, metricGroups);
    }

    public void readDocument(RuleSource source, Map<String, Concept> concepts, Map<String, Constraint> constraints, Map<String, String[]> dependencies) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Asciidoctor.STRUCTURE_MAX_LEVEL, 10);
        InputStream stream = null;
        try {
            stream = source.getInputStream();
        } catch (IOException e) {
            return;
        }
        StructuredDocument doc = asciidoctor.readDocumentStructure(new InputStreamReader(stream), parameters);
        /*
         * document info, DocumentHeader header = doc.getHeader();
         * System.out.println("Id: " + header.getId());
         * System.out.println("Title: " +extractTitle(doc, header, group)
         * System.out.println("Author: " + header.getAuthor());
         * System.out.println("Version: " +
         * header.getAttributes().get("version"));
         * System.out.println("Namespace: " +
         * header.getAttributes().get("namespace"));
         */

        gatherConcepts(doc, concepts, dependencies);
        gatherConstraints(doc, constraints, dependencies);

        resolveDependencies(dependencies, concepts, concepts);
        resolveDependencies(dependencies, constraints, concepts);
    }

    public String extractTitle(StructuredDocument doc, DocumentHeader header, Group group) {
        Title documentTitle = header.getDocumentTitle();
        if (documentTitle != null)
            return documentTitle.getCombined();
        String title = header.getPageTitle();
        if (title != null)
            return title;
        List<ContentPart> sections = doc.getPartsByContext("section");
        if (!sections.isEmpty()) {
            return sections.get(0).getTitle();
        }
        return null;
    }

    public void listSections(StructuredDocument doc) {
        System.out.println("\nSections: \n");
        List<ContentPart> partsByContext = doc.getPartsByContext("section");
        int index = 1;
        for (ContentPart section : partsByContext) {
            System.out.println("title: " + (index++) + "." + section.getTitle());
        }
    }

    private void resolveDependencies(Map<String, String[]> dependencies, Map<String, ? extends AbstractRule> rules, Map<String, Concept> concepts) {
        for (AbstractRule rule : rules.values()) {
            String[] dependingIds = dependencies.get(rule.getId());
            Set<Concept> dependingConcepts = resolveConcepts(concepts, dependingIds);
            if (dependingConcepts.isEmpty())
                continue;
            rule.setRequiresConcepts(dependingConcepts);
        }
    }

    private Set<Concept> resolveConcepts(Map<String, Concept> concepts, String[] ids) {
        if (ids == null || ids.length == 0)
            return Collections.emptySet();
        Set<Concept> result = new HashSet<>();
        for (String id : ids) {
            Concept c = concepts.get(id);
            if (c == null)
                continue;
            result.add(c);
        }
        return result;
    }

    private void gatherConcepts(StructuredDocument doc, Map<String, Concept> concepts, Map<String, String[]> dependencies) {
        for (ContentPart part : findListings(doc, "concept")) {
            Concept concept = populateRule(part, new Concept(), dependencies);
            concepts.put(concept.getId(), concept);
        }
    }

    private void gatherConstraints(StructuredDocument doc, Map<String, Constraint> constraints, Map<String, String[]> dependencies) {
        for (ContentPart part : findListings(doc, "constraint")) {
            Constraint constraint = populateRule(part, new Constraint(), dependencies);
            constraints.put(constraint.getId(), constraint);
        }
    }

    private <T extends AbstractRule> T populateRule(ContentPart part, T rule, Map<String, String[]> dependencies) {
        Map<String, Object> attributes = part.getAttributes();
        rule.setId(part.getId());
        rule.setDescription(attributes.get("title").toString());
        Query query = new Query();
        query.setCypher(unescapeHtml(part.getContent()));
        rule.setQuery(query);
        extractDependingIds(part, dependencies, attributes);
        // conceptRule.setSeverity();
        return rule;
    }

    // todo do better, or even better add a part.get(Original|Raw)Content() to
    // asciidoctor
    private String unescapeHtml(String content) {
        return content.replace("&lt;", "<").replace("&gt;", ">");
    }

    private void extractDependingIds(ContentPart part, Map<String, String[]> dependencies, Map<String, Object> attributes) {
        String depends = (String) attributes.get("depends");
        if (depends != null && !depends.trim().isEmpty()) {
            dependencies.put(part.getId(), depends.split("\\s*,\\s*"));
        }
    }

    private static Collection<ContentPart> findListings(StructuredDocument doc, String role) {
        Set<ContentPart> result = new LinkedHashSet<ContentPart>();
        result.addAll(findListings(doc.getParts(), role));
        return result;
    }

    private static Collection<ContentPart> findListings(Collection<ContentPart> parts, String role) {
        Set<ContentPart> result = new LinkedHashSet<ContentPart>();
        if (parts != null) {
            for (ContentPart part : parts) {
                if (role.equals(part.getRole()) && "listing".equals(part.getContext()) && "source".equals(part.getStyle())
                        && "cypher".equals(part.getAttributes().get("language"))) {
                    result.add(part);
                }
                result.addAll(findListings(part.getParts(), role));
            }
        }
        return result;
    }

    private static void dump(ContentPart contentPart) {
        Map<String, Object> attributes = contentPart.getAttributes();
        System.out.printf("Title: %s %n%nid '%s' context '%s' role '%s' style '%s' lang '%s' depends '%s'%n---%n%s%n", attributes.get("title"),
                contentPart.getId(), contentPart.getContext(), contentPart.getRole(), contentPart.getStyle(), attributes.get("language"),
                attributes.get("depends"), contentPart.getContent());
    }
}
