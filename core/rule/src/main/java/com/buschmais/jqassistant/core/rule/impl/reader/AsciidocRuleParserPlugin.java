package com.buschmais.jqassistant.core.rule.impl.reader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.rule.api.reader.AggregationVerification;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.impl.SourceExecutable;
import com.buschmais.jqassistant.core.shared.asciidoc.AsciidoctorFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.PreprocessorReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.asciidoctor.AttributesBuilder.attributes;
import static org.asciidoctor.OptionsBuilder.options;

/**
 * @author mh
 * @since 12.10.14
 */
public class AsciidocRuleParserPlugin extends AbstractRuleParserPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsciidocRuleParserPlugin.class);

    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("(.*?)(\\((.*)\\))?");

    private static final String CONCEPT = "concept";
    private static final String CONSTRAINT = "constraint";
    private static final String GROUP = "group";

    private static final Set<String> EXECUTABLE_RULE_TYPES = new HashSet<>(asList(CONCEPT, CONSTRAINT));

    private static final String INCLUDES_GROUPS = "includesGroups";
    private static final String INCLUDES_CONCEPTS = "includesConcepts";
    private static final String INCLUDES_CONSTRAINTS = "includesConstraints";

    private static final String SEVERITY = "severity";
    private static final String REQUIRES_CONCEPTS = "requiresConcepts";
    private static final String REQUIRES_PARAMETERS = "requiresParameters";
    private static final String REPORT_TYPE = "reportType";
    private static final String PRIMARY_REPORT_COLUM = "primaryReportColumn";
    private static final String REPORT_PROPERTIES = "reportProperties";
    private static final String VERIFY = "verify";
    private static final String AGGREGATION = "aggregation";
    private static final String AGGREGATION_COLUMN = "aggregationColumn";
    private static final String AGGREGATION_MIN = "aggregationMin";
    private static final String AGGREGATION_MAX = "aggregationMax";
    private static final String ROW_COUNT_MIN = "rowCountMin";
    private static final String ROW_COUNT_MAX = "rowCountMax";
    private static final String TITLE = "title";
    private static final String SOURCE = "source";
    private static final String LANGUAGE = "language";
    private static final String CYPHER = "cypher";
    private static final String OPTIONAL = "optional";

    private Asciidoctor asciidoctor = null;

    private Treeprocessor treeprocessor;

    private File tempDir;

    @Override
    public void initialize() throws RuleException {
        try {
            tempDir = Files.createTempDirectory("jQA").toFile();
        } catch (IOException e) {
            throw new RuleException("Cannot create temporary directory.");
        }
    }

    @Override
    public void destroy() throws RuleException {
        if (asciidoctor != null) {
            asciidoctor.shutdown();
        }
        try {
            FileUtils.deleteDirectory(tempDir);
        } catch (IOException e) {
            throw new RuleException("Cannot delete temporary directory: " + tempDir);
        }
    }

    @Override
    public boolean accepts(RuleSource ruleSource) {
        return ruleSource.getId().toLowerCase().endsWith(".adoc");
    }

    @Override
    protected void doParse(RuleSource source, RuleSetBuilder ruleSetBuilder) throws RuleException {
        String content;
        try (InputStream stream = source.getInputStream()) {
            content = IOUtils.toString(stream);
        } catch (IOException e) {
            throw new RuleException("Cannot parse AsciiDoc document from " + source.getId(), e);
        }
        OptionsBuilder optionsBuilder = options().mkDirs(true).safe(SafeMode.UNSAFE).baseDir(tempDir)
                .attributes(attributes().attribute(AsciidoctorFactory.ATTRIBUTE_IMAGES_OUT_DIR, tempDir.getAbsolutePath()).experimental(true));
        getAsciidoctor().load(content, optionsBuilder.asMap());
        extractRules(source, singletonList(treeprocessor.getDocument()), ruleSetBuilder);
    }

    /**
     * Returns an {@link Asciidoctor} instance which is created lazliy to reduce
     * startup time.
     *
     * @return The {@link Asciidoctor} instance.
     */
    private Asciidoctor getAsciidoctor() {
        if (asciidoctor == null) {
            asciidoctor = AsciidoctorFactory.getAsciidoctor();
            treeprocessor = new Treeprocessor();
            IgnoreIncludeProcessor includeProcessor = new IgnoreIncludeProcessor();
            JavaExtensionRegistry extensionRegistry = asciidoctor.javaExtensionRegistry();
            extensionRegistry.treeprocessor(treeprocessor);
            extensionRegistry.includeProcessor(includeProcessor);
        }
        return asciidoctor;
    }

    /**
     * Find all content parts representing source code listings with a role that
     * represents a rule.
     *
     *
     * @param ruleSource
     *            The rule source.
     * @param blocks
     *            The content parts of the document.
     * @param builder
     *            The {@link RuleSetBuilder}.
     */
    private void extractRules(RuleSource ruleSource, Collection<?> blocks, RuleSetBuilder builder) throws RuleException {
        for (Object element : blocks) {
            if (element instanceof AbstractBlock) {
                AbstractBlock block = (AbstractBlock) element;
                if (EXECUTABLE_RULE_TYPES.contains(block.getRole())) {
                    extractExecutableRule(ruleSource, block, builder);
                } else if (GROUP.equals(block.getRole())) {
                    extractGroup(ruleSource, block, builder);
                }
                extractRules(ruleSource, block.getBlocks(), builder);
            } else if (element instanceof Collection<?>) {
                extractRules(ruleSource, (Collection<?>) element, builder);
            }
        }
    }

    private void extractExecutableRule(RuleSource ruleSource, AbstractBlock executableRuleBlock, RuleSetBuilder builder) throws RuleException {
        Attributes attributes = new Attributes(executableRuleBlock.getAttributes());
        String id = executableRuleBlock.id();
        if (id == null) {
            throw new RuleException("An id attribute is required for the rule '" + executableRuleBlock + "' (i.e. '[[rule:id]]' is required.");
        }
        String description = attributes.getString(TITLE, "");
        if (description == null) {
            LOGGER.info("Description of rule is missing: Using empty text for description (source='{}', id='{}').", ruleSource.getId(), id);
        }
        Map<String, Boolean> required = getRequiresConcepts(ruleSource, id, attributes);
        Map<String, Parameter> parameters = getParameters(attributes.getString(REQUIRES_PARAMETERS));
        Executable<?> executable = getExecutable(executableRuleBlock, attributes);
        if (executable != null) {
            Verification verification = getVerification(attributes);
            Report report = getReport(executableRuleBlock);
            if (CONCEPT.equals(executableRuleBlock.getRole())) {
                Severity severity = getSeverity(attributes, getRuleConfiguration().getDefaultConceptSeverity());
                Concept concept = Concept.builder().id(id).description(description).severity(severity).executable(executable).requiresConcepts(required)
                        .parameters(parameters).verification(verification).report(report).ruleSource(ruleSource).build();
                builder.addConcept(concept);
            } else if (CONSTRAINT.equals(executableRuleBlock.getRole())) {
                Severity severity = getSeverity(attributes, getRuleConfiguration().getDefaultConstraintSeverity());
                Constraint constraint = Constraint.builder().id(id).description(description).severity(severity).executable(executable)
                        .requiresConcepts(required).parameters(parameters).verification(verification).report(report).ruleSource(ruleSource).build();
                builder.addConstraint(constraint);
            }
        }
    }

    private Executable<?> getExecutable(AbstractBlock block, Attributes attributes) {
        String language;
        if (SOURCE.equals(block.getStyle())) {
            language = attributes.getString(LANGUAGE);
            String source = unescapeHtml(block.getContent());
            if (CYPHER.equals(language)) {
                return new CypherExecutable(source);
            } else {
                return new ScriptExecutable(language.toLowerCase(), source);
            }
        } else {
            // Use style for native Asciidoc blocks
            language = block.getStyle();
            if (language == null) {
                // PlantUML extension
                language = (String) block.getAttributes().get(1);
            }
            if (language != null) {
                return new SourceExecutable<>(language.toLowerCase(), block, AbstractBlock.class);
            } else {
                LOGGER.warn("Cannot determine language for '" + block + "'.");
            }
        }
        return null;
    }

    private Verification getVerification(Attributes attributes) {
        if (AGGREGATION.equals(attributes.getString(VERIFY))) {
            return AggregationVerification.builder().column(attributes.getString(AGGREGATION_COLUMN)).min(attributes.getInt(AGGREGATION_MIN))
                    .max(attributes.getInt(AGGREGATION_MAX)).build();
        }
        Integer min = attributes.getInt(ROW_COUNT_MIN);
        Integer max = attributes.getInt(ROW_COUNT_MAX);
        if (min != null || max != null) {
            return RowCountVerification.builder().min(min).max(max).build();
        }
        return null;
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
     * @return A map where the keys represent the ids of required concepts and the
     *         values if they are optional.
     * @throws RuleException
     *             If the dependencies cannot be evaluated.
     */
    private Map<String, Boolean> getRequiresConcepts(RuleSource ruleSource, String id, Attributes attributes) throws RuleException {
        Map<String, String> requiresDeclarations = getReferences(attributes, REQUIRES_CONCEPTS);
        Map<String, Boolean> required = new HashMap<>();
        for (Map.Entry<String, String> requiresEntry : requiresDeclarations.entrySet()) {
            String conceptId = requiresEntry.getKey();
            String dependencyAttribute = requiresEntry.getValue();
            Boolean optional = dependencyAttribute != null ? OPTIONAL.equals(dependencyAttribute.toLowerCase()) : null;
            required.put(conceptId, optional);
        }
        return required;
    }

    private void extractGroup(RuleSource ruleSource, AbstractBlock groupBlock, RuleSetBuilder ruleSetBuilder) throws RuleException {
        Attributes attributes = new Attributes(groupBlock.getAttributes());
        Map<String, Severity> constraints = getGroupElements(attributes, INCLUDES_CONSTRAINTS);
        Map<String, Severity> concepts = getGroupElements(attributes, INCLUDES_CONCEPTS);
        Map<String, Severity> groups = getGroupElements(attributes, INCLUDES_GROUPS);
        Severity severity = getSeverity(attributes, getRuleConfiguration().getDefaultGroupSeverity());
        Group group = Group.builder().id(groupBlock.id()).description(groupBlock.getTitle()).severity(severity).ruleSource(ruleSource).concepts(concepts)
                .constraints(constraints).groups(groups).build();
        ruleSetBuilder.addGroup(group);
    }

    private Map<String, Severity> getGroupElements(Attributes attributes, String attributeName) throws RuleException {
        Map<String, String> references = getReferences(attributes, attributeName);
        Map<String, Severity> result = new HashMap<>();
        for (Map.Entry<String, String> entry : references.entrySet()) {
            String id = entry.getKey();
            String dependencyAttribute = entry.getValue();
            Severity severity = dependencyAttribute != null ? Severity.fromValue(dependencyAttribute.toLowerCase()) : null;
            result.put(id, severity);
        }
        return result;
    }

    /**
     * Get reference declarations for an attribute from a map of attributes.
     *
     * @param attributes
     *            The map of attributes.
     * @param attributeName
     *            The name of the attribute.
     * @return A map containing the ids of the references as keys and their
     *         associated values (optional).
     */
    private Map<String, String> getReferences(Attributes attributes, String attributeName) {
        String attribute = attributes.getString(attributeName);
        Set<String> references = new HashSet<>();
        if (attribute != null && !attribute.trim().isEmpty()) {
            references.addAll(asList(attribute.split("\\s*,\\s*")));
        }
        Map<String, String> rules = new HashMap<>();
        for (String reference : references) {
            Matcher matcher = DEPENDENCY_PATTERN.matcher(reference);
            if (matcher.matches()) {
                String id = matcher.group(1);
                String referenceValue = matcher.group(3);
                rules.put(id, referenceValue);
            }
        }
        return rules;
    }

    /**
     * Extract the optional severity of a rule.
     *
     * @param attributes
     *            The attributes of the rule.
     * @param defaultSeverity
     *            The default severity to use if no severity is specified.
     * @return The severity.
     */
    private Severity getSeverity(Attributes attributes, Severity defaultSeverity) throws RuleException {
        String severity = attributes.getString(SEVERITY);
        if (severity == null) {
            return defaultSeverity;
        }
        Severity value = Severity.fromValue(severity.toLowerCase());
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
    private String unescapeHtml(Object content) {
        return content != null ? content.toString().replace("&lt;", "<").replace("&gt;", ">") : "";
    }

    /**
     * Create the report part of a rule.
     *
     * @param part
     *            The content part.
     * @return The report.
     */
    private Report getReport(AbstractBlock part) {
        Object primaryReportColum = part.getAttributes().get(PRIMARY_REPORT_COLUM);
        Object reportType = part.getAttributes().get(REPORT_TYPE);
        Properties reportProperties = parseProperties(part, REPORT_PROPERTIES);
        Report.ReportBuilder reportBuilder = Report.builder();
        if (reportType != null) {
            reportBuilder.selectedTypes(Report.selectTypes(reportType.toString()));
        }
        if (primaryReportColum != null) {
            reportBuilder.primaryColumn(primaryReportColum.toString());
        }
        return reportBuilder.properties(reportProperties).build();
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
    private Properties parseProperties(AbstractBlock part, String attributeName) {
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

    private static final class Attributes {

        private final Map<String, Object> attributes;

        private Attributes(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        private Integer getInt(String key) {
            Object value = attributes.get(key);
            if (value != null) {
                return Integer.valueOf(value.toString());
            }
            return null;
        }

        private String getString(String key) {
            return getString(key, null);
        }

        private String getString(String key, String defaultValue) {
            Object value = attributes.get(key);
            if (value != null) {
                return value.toString();
            }
            return defaultValue;
        }
    }

    /**
     * Used to retrieve the "raw" document.
     */
    private class Treeprocessor extends org.asciidoctor.extension.Treeprocessor {

        private Document document;

        public Treeprocessor() {
        }

        @Override
        public Document process(Document document) {
            this.document = document;
            return document;
        }

        public Document getDocument() {
            return document;
        }
    }

    /**
     * Include processor that ignores all included files.
     */
    private class IgnoreIncludeProcessor extends IncludeProcessor {

        @Override
        public boolean handles(String target) {
            return true;
        }

        @Override
        public void process(DocumentRuby document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
            LOGGER.debug("Skipping included file '{}'.", target);
        }

    }
}
