package com.buschmais.jqassistant.core.analysis.impl;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.ContentPart;
import org.asciidoctor.ast.StructuredDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.api.RuleException;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;

/**
 * @author mh
 * @since 12.10.14
 */
public class AsciiDocRuleSetReader implements RuleSetReader {

    private static final Set<String> EXECUTABLE_RULE_TYPES = new HashSet<>(asList("concept", "constraint"));

    private static final Logger LOGGER = LoggerFactory.getLogger(AsciiDocRuleSetReader.class);

    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("(.*?)(\\((.*)\\))?");

    /**
     * The cached rule set reader, initialized lazily.
     */
    private Asciidoctor cachedAsciidoctor = null;

    @Override
    public void read(List<? extends RuleSource> sources, RuleSetBuilder ruleSetBuilder) throws RuleException {
        for (RuleSource source : sources) {
            if (source.isType(RuleSource.Type.AsciiDoc)) {
                readDocument(source, ruleSetBuilder);
            }
        }
    }

    /**
     * Reads and decodes a rule source.
     * 
     * @param source
     *            The source.
     * @param builder
     *            The builder to use.
     * @throws RuleException
     *             If building fails.
     */
    private void readDocument(RuleSource source, RuleSetBuilder builder) throws RuleException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Asciidoctor.STRUCTURE_MAX_LEVEL, 10);
        InputStream stream;
        try {
            stream = source.getInputStream();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read rules from '" + source.getId() + "'.", e);
        }
        StructuredDocument doc = getAsciidoctor().readDocumentStructure(new InputStreamReader(stream), parameters);
        extractExecutableRules(source, doc, builder);
        extractGroups(source, doc, builder);
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

    private void extractExecutableRules(RuleSource ruleSource, StructuredDocument doc, RuleSetBuilder builder) throws RuleException {
        for (ContentPart part : findExecutableRules(doc.getParts())) {
            Map<String, Object> attributes = part.getAttributes();
            String id = part.getId();
            String description = attributes.get("title").toString();
            Set<String> requiresConcepts = new HashSet<>(getDependencies(attributes, "requiresConcepts").keySet());
            Set<String> depends = getDependencies(attributes, "depends").keySet();
            if (!depends.isEmpty()) {
                LOGGER.info("Using 'depends' to reference required concepts is deprecated, please use 'requiresConcepts' (source='{}', id='{}'}.",
                        ruleSource.getId(), id);
                requiresConcepts.addAll(depends);
            }
            Object language = part.getAttributes().get("language");
            String source = unescapeHtml(part.getContent());
            Executable executable;
            if ("cypher".equals(language)) {
                executable = new CypherExecutable(source);
            } else {
                executable = new ScriptExecutable(language.toString(), source);
            }
            Verification verification;
            boolean aggregation = "aggregation".equals(part.getAttributes().get("verify"));
            if (aggregation) {
                Object aggregationColumn = part.getAttributes().get("aggregationColumn");
                verification = new AggregationVerification(aggregationColumn != null ? aggregationColumn.toString() : null);
            } else {
                verification = new RowCountVerification();
            }
            Object primaryReportColum = part.getAttributes().get("primaryReportColum");
            Report report = new Report(primaryReportColum != null ? primaryReportColum.toString() : null);
            if ("concept".equals(part.getRole())) {
                Severity severity = getSeverity(part, Concept.DEFAULT_SEVERITY);
                Concept concept = new Concept(id, description, ruleSource, severity, null, executable, Collections.<String, Object> emptyMap(),
                        requiresConcepts, verification, report);
                builder.addConcept(concept);
            } else if ("constraint".equals(part.getRole())) {
                Severity severity = getSeverity(part, Constraint.DEFAULT_SEVERITY);
                Constraint constraint = new Constraint(id, description, ruleSource, severity, null, executable, Collections.<String, Object> emptyMap(),
                        requiresConcepts, verification, report);
                builder.addConstraint(constraint);
            }
        }
    }

    /**
     * Extract the defined groups from a document.
     * 
     * @param ruleSource
     *            The source of the document.
     * @param doc
     *            The document.
     * @param ruleSetBuilder
     *            The rule set builder.
     * @throws RuleException
     *             If the rules cannot be built.
     */
    private void extractGroups(RuleSource ruleSource, StructuredDocument doc, RuleSetBuilder ruleSetBuilder) throws RuleException {
        for (ContentPart contentPart : findGroups(doc.getParts())) {
            Map<String, Object> attributes = contentPart.getAttributes();
            Map<String, Severity> constraints = getDependencies(attributes, "includesConstraints");
            Map<String, Severity> concepts = getDependencies(attributes, "includesConcepts");
            Set<String> groups = getDependencies(attributes, "includesGroups").keySet();
            Group group = new Group(contentPart.getId(), contentPart.getTitle(), ruleSource, concepts, constraints, groups);
            ruleSetBuilder.addGroup(group);
        }
    }

    /**
     * Get dependency declarations for an attribute from a map of attributes.
     * 
     * @param attributes
     *            The map of attributes.
     * @param attributeName
     *            The name of the attribute.
     * @return A map containing the ids of the dependencies as keys and their
     *         severity (optional).
     */
    private Map<String, Severity> getDependencies(Map<String, Object> attributes, String attributeName) {
        String attribute = (String) attributes.get(attributeName);
        Set<String> dependencies = new HashSet<>();
        if (attribute != null && !attribute.trim().isEmpty()) {
            dependencies.addAll(asList(attribute.split("\\s*,\\s*")));
        }
        Map<String, Severity> rules = new HashMap<>();
        for (String dependency : dependencies) {
            Matcher matcher = DEPENDENCY_PATTERN.matcher(dependency);
            if (matcher.matches()) {
                String id = matcher.group(1);
                String severityValue = matcher.group(3);
                Severity severity = severityValue != null ? Severity.fromValue(severityValue.toLowerCase()) : null;
                rules.put(id, severity);
            }
        }
        return rules;
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
     * TODO do better, or even better add a partget(Original|Raw)Content() to
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
     * Find all content parts representing source code listings with a role that
     * represents a rule.
     * 
     * @param parts
     *            The content parts of the document.
     * @return A collection of content parts representing rules.
     */
    private static Collection<ContentPart> findExecutableRules(Collection<ContentPart> parts) {
        Set<ContentPart> result = new LinkedHashSet<ContentPart>();
        if (parts != null) {
            for (ContentPart part : parts) {
                if ("listing".equals(part.getContext()) && "source".equals(part.getStyle()) && EXECUTABLE_RULE_TYPES.contains(part.getRole())) {
                    result.add(part);
                }
                result.addAll(findExecutableRules(part.getParts()));
            }
        }
        return result;
    }

    private static Collection<ContentPart> findGroups(Collection<ContentPart> parts) {
        Set<ContentPart> result = new LinkedHashSet<>();
        if (parts != null) {
            for (ContentPart part : parts) {
                if ("group".equals(part.getRole())) {
                    result.add(part);
                }
                result.addAll(findGroups(part.getParts()));
            }
        }
        return result;
    }
}
