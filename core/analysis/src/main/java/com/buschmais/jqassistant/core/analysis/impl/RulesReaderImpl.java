package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.RulesReader;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.*;
import com.buschmais.jqassistant.core.model.api.Query;
import com.buschmais.jqassistant.core.model.api.rules.Concept;
import com.buschmais.jqassistant.core.model.api.rules.Constraint;
import com.buschmais.jqassistant.core.model.api.rules.ConstraintGroup;
import com.buschmais.jqassistant.core.model.api.rules.RuleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import java.util.*;

/**
 * A {@link RulesReader} implementation.
 */
public class RulesReaderImpl implements RulesReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(RulesReaderImpl.class);

    private JAXBContext jaxbContext;

    /**
     * Constructor.
     */
    public RulesReaderImpl() {
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot create JAXB context.", e);
        }
    }

    @Override
    public RuleSet read(List<Source> sources) {
        LOGGER.debug("Reading rules from {} sources.", Integer.toString(sources.size()));
        List<JqassistantRules> rules = new ArrayList<JqassistantRules>();
        for (Source source : sources) {
            try {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                unmarshaller.setSchema(JaxbHelper.getSchema("/META-INF/xsd/jqassistant-rules-1.0.xsd"));
                rules.add(unmarshaller.unmarshal(source, JqassistantRules.class).getValue());
            } catch (JAXBException e) {
                throw new IllegalArgumentException("Cannot read rules : " + source.toString(), e);
            }
        }
        return convert(rules);
    }

    /**
     * Converts a list of {@link JqassistantRules} to a {@link RuleSet}.
     *
     * @param rules The {@link JqassistantRules}.
     * @return The corresponding {@link RuleSet}.
     */
    private RuleSet convert(List<JqassistantRules> rules) {
        Map<String, QueryDefinitionType> queryDefinitionTypes = new HashMap<String, QueryDefinitionType>();
        Map<String, ConceptType> conceptTypes = new HashMap<String, ConceptType>();
        Map<String, ConstraintType> constraintTypes = new HashMap<String, ConstraintType>();
        Map<String, ConstraintGroupType> constraintGroupTypes = new HashMap<String, ConstraintGroupType>();
        for (JqassistantRules rule : rules) {
            cacheXmlTypes(rule.getQueryDefinition(), queryDefinitionTypes);
            cacheXmlTypes(rule.getConcept(), conceptTypes);
            cacheXmlTypes(rule.getConstraint(), constraintTypes);
            cacheXmlTypes(rule.getConstraintGroup(), constraintGroupTypes);
        }
        RuleSet ruleSet = new RuleSet();
        readConcepts(queryDefinitionTypes, conceptTypes, ruleSet);
        readConstraints(queryDefinitionTypes, conceptTypes, constraintTypes, ruleSet);
        readConstraintGroups(constraintTypes, constraintGroupTypes, ruleSet);
        return ruleSet;
    }

    /**
     * Caches the given XML types in the provided {@link Map}.
     *
     * @param list    The XML types.
     * @param typeMap The {@link Map}.
     * @param <T>     The value type of the {@link Map}.
     */
    private <T extends ReferenceableType> void cacheXmlTypes(List<T> list, Map<String, T> typeMap) {
        for (T t : list) {
            typeMap.put(t.getId(), t);
        }
    }

    /**
     * Reads {@link ConceptType}s and converts them to {@link Concept}s.
     *
     * @param queryDefinitionTypes The {@link QueryDefinitionType}s.
     * @param conceptTypes         The {@link ConceptType}s.
     * @param ruleSet              The {@link RuleSet}.
     */
    private void readConcepts(Map<String, QueryDefinitionType> queryDefinitionTypes, Map<String, ConceptType> conceptTypes, RuleSet ruleSet) {
        for (ConceptType conceptType : conceptTypes.values()) {
            Concept concept = getOrCreateConcept(conceptType.getId(), ruleSet.getConcepts());
            concept.setDescription(conceptType.getDescription());
            if (conceptType.getUseQueryDefinition() != null) {
                concept.setQuery(createQueryFromDefinition(conceptType.getUseQueryDefinition().getRefId(), conceptType.getParameter(), queryDefinitionTypes));
            } else {
                concept.setQuery(createQuery(conceptType.getCypher(), conceptType.getParameter()));
            }
            concept.setRequiredConcepts(getRequiredConcepts(conceptType.getRequiresConcept(), conceptTypes, ruleSet.getConcepts()));
        }
    }

    /**
     * Reads {@link ConstraintType}s and converts them to {@link Constraint}s.
     *
     * @param queryDefinitionTypes The {@link QueryDefinitionType}s.
     * @param conceptTypes         The {@link ConceptType}s.
     * @param constraintTypes      The {@link ConstraintType}s.
     * @param ruleSet              The {@link RuleSet}.
     */
    private void readConstraints(Map<String, QueryDefinitionType> queryDefinitionTypes, Map<String, ConceptType> conceptTypes, Map<String, ConstraintType> constraintTypes, RuleSet ruleSet) {
        for (ConstraintType constraintType : constraintTypes.values()) {
            Constraint constraint = getOrCreateConstraint(constraintType.getId(), ruleSet.getConstraints());
            constraint.setDescription(constraintType.getDescription());
            if (constraintType.getUseQueryDefinition() != null) {
                constraint.setQuery(createQueryFromDefinition(constraintType.getUseQueryDefinition().getRefId(), constraintType.getParameter(), queryDefinitionTypes));
            } else {
                constraint.setQuery(createQuery(constraintType.getCypher(), constraintType.getParameter()));
            }
            constraint.setRequiredConcepts(getRequiredConcepts(constraintType.getRequiresConcept(), conceptTypes, ruleSet.getConcepts()));
        }
    }

    /**
     * Reads {@link ConstraintGroupType}s and converts them to {@link ConstraintGroup}s.
     *
     * @param constraintTypes      The {@link ConstraintType}s.
     * @param constraintGroupTypes The {@link ConstraintGroupType}s.
     * @param ruleSet              The {@link RuleSet}.
     */
    private void readConstraintGroups(Map<String, ConstraintType> constraintTypes, Map<String, ConstraintGroupType> constraintGroupTypes, RuleSet ruleSet) {
        for (ConstraintGroupType constraintGroupType : constraintGroupTypes.values()) {
            ConstraintGroup constraintGroup = getOrCreateConstraintGroup(constraintGroupType.getId(), ruleSet.getConstraintGroups());
            for (ReferenceType referenceType : constraintGroupType.getIncludeConstraint()) {
                ConstraintType includedConstraintType = constraintTypes.get(referenceType.getRefId());
                if (includedConstraintType == null) {
                    throw new IllegalArgumentException("Cannot resolve included constraint: " + referenceType.getRefId());
                }
                constraintGroup.getConstraints().add(getOrCreateConstraint(referenceType.getRefId(), ruleSet.getConstraints()));
            }
            for (ReferenceType referenceType : constraintGroupType.getIncludeConstraintGroup()) {
                ConstraintGroupType includedConstraintType = constraintGroupTypes.get(referenceType.getRefId());
                if (includedConstraintType == null) {
                    throw new IllegalArgumentException("Cannot resolve included constraint group: " + referenceType.getRefId());
                }
                constraintGroup.getConstraintGroups().add(getOrCreateConstraintGroup(referenceType.getRefId(), ruleSet.getConstraintGroups()));
            }
        }
    }

    /**
     * Gets a {@link Concept} from the cache or create a new instance if it does not exist yet.
     *
     * @param id       The id.
     * @param concepts The {@link Concept}s.
     * @return The {@link Concept}.
     */
    private Concept getOrCreateConcept(String id, Map<String, Concept> concepts) {
        Concept concept = concepts.get(id);
        if (concept == null) {
            concept = new Concept();
            concept.setId(id);
            concepts.put(id, concept);
        }
        return concept;
    }

    /**
     * Gets a {@link Constraint} from the cache or create a new instance if it does not exist yet.
     *
     * @param id          The id.
     * @param constraints The {@link Constraint}s.
     * @return The {@link Constraint}.
     */
    private Constraint getOrCreateConstraint(String id, Map<String, Constraint> constraints) {
        Constraint constraint = constraints.get(id);
        if (constraint == null) {
            constraint = new Constraint();
            constraint.setId(id);
            constraints.put(id, constraint);
        }
        return constraint;
    }

    /**
     * Gets a {@link ConstraintGroup} from the cache or create a new instance if it does not exist yet.
     *
     * @param id               The id.
     * @param constraintGroups The {@link ConstraintGroup}s.
     * @return The {@link ConstraintGroup}.
     */
    private ConstraintGroup getOrCreateConstraintGroup(String id, Map<String, ConstraintGroup> constraintGroups) {
        ConstraintGroup constraintGroup = constraintGroups.get(id);
        if (constraintGroup == null) {
            constraintGroup = new ConstraintGroup();
            constraintGroup.setId(id);
            constraintGroups.put(id, constraintGroup);
        }
        return constraintGroup;
    }

    /**
     * Resolves the required {@link Concept}s for a given {@link ReferenceType}.
     *
     * @param referenceTypes The {@link ReferenceType}s.
     * @param conceptTypes   The {@link ConceptType}s.
     * @param concepts       The {@link Concept}s.
     * @return The required {@link Concept}s.
     */
    private Set<Concept> getRequiredConcepts(List<ReferenceType> referenceTypes, Map<String, ConceptType> conceptTypes, Map<String, Concept> concepts) {
        Set<Concept> requiredConcepts = new HashSet<Concept>();
        for (ReferenceType referenceType : referenceTypes) {
            ConceptType requiredConceptType = conceptTypes.get(referenceType.getRefId());
            if (requiredConceptType == null) {
                throw new IllegalArgumentException("Cannot resolve required concept: " + referenceType.getRefId());
            }
            requiredConcepts.add(getOrCreateConcept(referenceType.getRefId(), concepts));
        }
        return requiredConcepts;
    }

    /**
     * Creates a {@link Query}.
     *
     * @param cypher         The CYPHER expression.
     * @param parameterTypes The {@link ParameterType}s.
     * @return The {@link Query}.
     */
    private Query createQuery(String cypher, List<ParameterType> parameterTypes) {
        Map<String, Object> defaultValues = Collections.emptyMap();
        return createQuery(cypher, parameterTypes, defaultValues);
    }

    /**
     * Creates a {@link Query} from a {@link QueryDefinitionType} identified by its id.
     *
     * @param id                   The id of the {@link QueryDefinitionType}.
     * @param parameterTypes       The {@link ParameterType}s.
     * @param queryDefinitionTypes The {@link QueryDefinitionType}s.
     * @return The {@link Query}.
     */
    private Query createQueryFromDefinition(String id, List<ParameterType> parameterTypes, Map<String, QueryDefinitionType> queryDefinitionTypes) {
        QueryDefinitionType queryDefinitionType = queryDefinitionTypes.get(id);
        if (queryDefinitionType == null) {
            throw new IllegalArgumentException("Cannot resolve used query definition: " + id);
        }
        Map<String, Object> defaultValues = new HashMap<String, Object>();
        for (ParameterDefinitionType parameterDefinitionType : queryDefinitionType.getParameterDefinition()) {
            defaultValues.put(parameterDefinitionType.getName(), getValue(parameterDefinitionType.getType(), parameterDefinitionType.getDefault()));
        }
        return createQuery(queryDefinitionType.getCypher(), parameterTypes, defaultValues);
    }

    /**
     * Creates a {@link Query}.
     *
     * @param cypher         The CYPHER expression.
     * @param parameterTypes The {@link ParameterType}s.
     * @param defaultValues  The default values to use.
     * @return The {@link Query}.
     */
    private Query createQuery(String cypher, List<ParameterType> parameterTypes, Map<String, Object> defaultValues) {
        Query query = new Query();
        query.setCypher(cypher);
        for (ParameterType parameterType : parameterTypes) {
            query.getParameters().put(parameterType.getName(), getValue(parameterType.getType(), parameterType.getValue()));
        }
        return query;
    }

    /**
     * Get a parameter value by its string representation and type.
     *
     * @param type        The {@link ParameterType}.
     * @param stringValue The string representation.
     * @return The parameter value.
     */
    private Object getValue(ParameterTypes type, String stringValue) {
        Object value;
        switch (type) {
            case INT:
                value = Integer.valueOf(stringValue);
                break;
            case STRING:
                value = stringValue;
                break;
            default:
                throw new IllegalArgumentException("Unsupported parameter type: " + type);
        }
        return value;
    }
}
