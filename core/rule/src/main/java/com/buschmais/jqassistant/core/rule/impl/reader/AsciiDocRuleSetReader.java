package com.buschmais.jqassistant.core.rule.impl.reader;

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

import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleSetReader;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

/**
 * @author mh
 * @since 12.10.14
 */
public class AsciiDocRuleSetReader implements RuleSetReader {

    private static final Set<String> EXECUTABLE_RULE_TYPES = new HashSet<>(asList("concept", "constraint"));

    private static final Logger LOGGER = LoggerFactory.getLogger(AsciiDocRuleSetReader.class);

    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("(.*?)(\\((.*)\\))?");

    public static final String CONCEPT = "concept";
    public static final String CONSTRAINT = "constraint";
    public static final String GROUP = "group";

    public static final String INCLUDES_GROUPS = "includesGroups";
    public static final String INCLUDES_CONCEPTS = "includesConcepts";
    public static final String INCLUDES_CONSTRAINTS = "includesConstraints";

    public static final String SEVERITY = "severity";
    public static final String DEPENDS = "depends";
    public static final String REQUIRES_CONCEPTS = "requiresConcepts";
    public static final String REQUIRES_PARAMETERS = "requiresParameters";
    public static final String REPORT_TYPE = "reportType";
    public static final String PRIMARY_REPORT_COLUM = "primaryReportColumn";
    public static final String REPORT_PROPERTIES = "reportProperties";
    public static final String VERIFY = "verify";
    public static final String AGGREGATION = "aggregation";
    public static final String AGGREGATION_COLUMN = "aggregationColumn";
    public static final String TITLE = "title";
    public static final String LISTING = "listing";
    public static final String SOURCE = "source";
    public static final String LANGUAGE = "language";
    public static final String CYPHER = "cypher";
    public static final String OPTIONAL = "optional";

    private RuleConfiguration ruleConfiguration;

    /**
     * The cached rule set reader, initialized lazily.
     */
    private Asciidoctor cachedAsciidoctor = null;

    public AsciiDocRuleSetReader(RuleConfiguration ruleConfiguration) {
        this.ruleConfiguration = ruleConfiguration;
    }

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
     * Return an Asciidoctor instance.
     *
     * Initialization is quite expensive, therefore doing it lazy.
     *
     * @return The Asciidoctor instance.
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
            String description = "";
            Object title = attributes.get(TITLE);
            if (title != null) {
                description = title.toString();
            } else {
                LOGGER.info("Description of rule is missing: Using empty text for description (source='{}', id='{}').", ruleSource.getId(), id);
            }
            Map<String, Boolean> required = getRequiresConcepts(ruleSource, id, attributes);

            Map<String, Parameter> parameters = getParameters(part.getAttributes().get(REQUIRES_PARAMETERS));
            Object language = part.getAttributes().get(LANGUAGE);
            String source = unescapeHtml(part.getContent());
            Executable executable;
            if (CYPHER.equals(language)) {
                executable = new CypherExecutable(source);
            } else {
                executable = new ScriptExecutable(language.toString(), source);
            }
            Verification verification;
            boolean aggregation = AGGREGATION.equals(part.getAttributes().get(VERIFY));
            if (aggregation) {
                Object aggregationColumn = part.getAttributes().get(AGGREGATION_COLUMN);
                verification = new AggregationVerification(aggregationColumn != null ? aggregationColumn.toString() : null);
            } else {
                verification = new RowCountVerification();
            }
            Report report = getReport(part);
            if (CONCEPT.equals(part.getRole())) {
                Severity severity = getSeverity(part, ruleConfiguration.getDefaultConceptSeverity());
                Concept concept = Concept.Builder.newConcept().id(id).description(description).severity(severity).executable(executable)
                        .requiresConceptIds(required).parameters(parameters).verification(verification).report(report).get();
                builder.addConcept(concept);
            } else if (CONSTRAINT.equals(part.getRole())) {
                Severity severity = getSeverity(part, ruleConfiguration.getDefaultConstraintSeverity());
                Constraint constraint = Constraint.Builder.newConstraint().id(id).description(description).severity(severity).executable(executable)
                        .requiresConceptIds(required).parameters(parameters).verification(verification).report(report).get();
                builder.addConstraint(constraint);
            }
        }
    }

    /**
     * Evaluates required concepts of a rule.
     * 
     * @param ruleSource
     *            The rule source.
     * @param attributes
     *            The attributes of an asciidoc rule block
     * @param id
     *            The id.
     * @return A map where the keys represent the ids of required concepts and
     *         the values if they are optional.
     * @throws RuleException
     *             If the dependencies cannot be evaluated.
     */
    private Map<String, Boolean> getRequiresConcepts(RuleSource ruleSource, String id, Map<String, Object> attributes) throws RuleException {
        Map<String, String> requiresDeclarations = getDependencyDeclarations(attributes, REQUIRES_CONCEPTS);
        Map<String, String> depends = getDependencyDeclarations(attributes, DEPENDS);
        if (!depends.isEmpty()) {
            LOGGER.info("Using 'depends' to reference required concepts is deprecated, please use 'requiresConcepts' (source='{}', id='{}').",
                    ruleSource.getId(), id);
            requiresDeclarations.putAll(depends);
        }
        Map<String, Boolean> required = new HashMap<>();
        for (Map.Entry<String, String> requiresEntry : requiresDeclarations.entrySet()) {
            String conceptId = requiresEntry.getKey();
            String dependencyAttribute = requiresEntry.getValue();
            Boolean optional = dependencyAttribute != null ? OPTIONAL.equals(dependencyAttribute.toLowerCase()) : null;
            required.put(conceptId, optional);
        }
        return required;
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
            Map<String, Severity> constraints = getGroupElements(attributes, INCLUDES_CONSTRAINTS);
            Map<String, Severity> concepts = getGroupElements(attributes, INCLUDES_CONCEPTS);
            Map<String, Severity> groups = getGroupElements(attributes, INCLUDES_GROUPS);
            Severity severity = getSeverity(contentPart, ruleConfiguration.getDefaultGroupSeverity());
            Group group = Group.Builder.newGroup().id(contentPart.getId()).description(contentPart.getTitle()).severity(severity).ruleSource(ruleSource)
                    .conceptIds(concepts).constraintIds(constraints).groupIds(groups).get();
            ruleSetBuilder.addGroup(group);
        }
    }

    private Map<String, Severity> getGroupElements(Map<String, Object> attributes, String attributeName) throws RuleException {
        Map<String, String> dependencyDeclarations = getDependencyDeclarations(attributes, attributeName);
        Map<String, Severity> result = new HashMap<>();
        for (Map.Entry<String, String> entry : dependencyDeclarations.entrySet()) {
            String id = entry.getKey();
            String dependencyAttribute = entry.getValue();
            Severity severity = dependencyAttribute != null ? Severity.fromValue(dependencyAttribute.toLowerCase()) : null;
            result.put(id, severity);
        }
        return result;
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
    private Map<String, String> getDependencyDeclarations(Map<String, Object> attributes, String attributeName) throws RuleException {
        String attribute = (String) attributes.get(attributeName);
        Set<String> dependencies = new HashSet<>();
        if (attribute != null && !attribute.trim().isEmpty()) {
            dependencies.addAll(asList(attribute.split("\\s*,\\s*")));
        }
        Map<String, String> rules = new HashMap<>();
        for (String dependency : dependencies) {
            Matcher matcher = DEPENDENCY_PATTERN.matcher(dependency);
            if (matcher.matches()) {
                String id = matcher.group(1);
                String dependencyAttribute = matcher.group(3);
                rules.put(id, dependencyAttribute);
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
    private Severity getSeverity(ContentPart part, Severity defaultSeverity) throws RuleException {
        Object severity = part.getAttributes().get(SEVERITY);
        if (severity == null) {
            return defaultSeverity;
        }
        Severity value = Severity.fromValue(severity.toString().toLowerCase());
        return value != null ? value : defaultSeverity;
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
        Set<ContentPart> result = new LinkedHashSet<>();
        if (parts != null) {
            for (ContentPart part : parts) {
                if (LISTING.equals(part.getContext()) && SOURCE.equals(part.getStyle()) && EXECUTABLE_RULE_TYPES.contains(part.getRole())) {
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
                if (GROUP.equals(part.getRole())) {
                    result.add(part);
                }
                result.addAll(findGroups(part.getParts()));
            }
        }
        return result;
    }

    /**
     * Create the report part of a rule.
     *
     * @param part
     *            The content part.
     * @return The report.
     */
    private Report getReport(ContentPart part) {
        Object primaryReportColum = part.getAttributes().get(PRIMARY_REPORT_COLUM);
        Object reportType = part.getAttributes().get(REPORT_TYPE);
        Properties reportProperties = parseProperties(part, REPORT_PROPERTIES);
        Report.Builder reportBuilder = Report.Builder.newInstance();
        if (reportType != null) {
            reportBuilder.selectTypes(reportType.toString());
        }
        if (primaryReportColum != null) {
            reportBuilder.primaryColumn(primaryReportColum.toString());
        }
        return reportBuilder.properties(reportProperties).get();
    }

    /**
     * Parse properties from an attribute.
     *
     * @param part
     *            The content part containing the attribute.
     * @param attributeName
     *            The attribute name.
     * @return The properties.
     */
    private Properties parseProperties(ContentPart part, String attributeName) {
        Properties properties = new Properties();
        Object attribute = part.getAttributes().get(attributeName);
        if (attribute == null) {
            return properties;
        }
        Scanner propertiesScanner = new Scanner(attribute.toString());
        propertiesScanner.useDelimiter(";");
        while (propertiesScanner.hasNext()) {
            String next = propertiesScanner.next().trim();
            if (next.length() > 0) {
                Scanner propertyScanner = new Scanner(next);
                propertyScanner.useDelimiter("=");
                String key = propertyScanner.next().trim();
                String value = propertyScanner.next().trim();
                properties.setProperty(key, value);
            }
        }
        return properties;
    }

    private Map<String, Parameter> getParameters(Object attribute) {
        if (attribute == null) {
            return Collections.emptyMap();
        }
        Map<String, Parameter> parameters = new HashMap<>();
        String[] parameterDeclarations = ((String) attribute).split(";");
        for (String parameterDeclaration : parameterDeclarations) {
            Scanner scanner = new Scanner(parameterDeclaration);
            String typeName = scanner.next();
            String name = scanner.next();
            Parameter.Type type = Parameter.Type.valueOf(typeName.toUpperCase());
            Parameter parameter = new Parameter(name, type, null);
            parameters.put(name, parameter);
        }
        return parameters;
    }

}
