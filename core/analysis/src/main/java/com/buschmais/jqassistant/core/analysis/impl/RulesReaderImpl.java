package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.RulesReader;
import com.buschmais.jqassistant.core.analysis.api.model.Concept;
import com.buschmais.jqassistant.core.analysis.api.model.Constraint;
import com.buschmais.jqassistant.core.analysis.api.model.ConstraintGroup;
import com.buschmais.jqassistant.core.analysis.api.model.Query;
import com.buschmais.jqassistant.core.analysis.rules.schema.v1.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 21.06.13
 * Time: 21:08
 * To change this template use File | Settings | File Templates.
 */
public class RulesReaderImpl implements RulesReader {

    public Map<String, ConstraintGroup> read(List<Source> sources) {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot create JAXB context.", e);
        }
        List<JqassistantRules> rules = new ArrayList<JqassistantRules>();
        for (Source source : sources) {
            try {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                unmarshaller.setSchema(getSchema());
                rules.add(unmarshaller.unmarshal(source, JqassistantRules.class).getValue());
            } catch (JAXBException e) {
                throw new IllegalArgumentException("Cannot read rules : " + source.toString(), e);
            }
        }
        return convert(rules);
    }

    private Schema getSchema() {
        Schema schema;
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = schemaFactory.newSchema(RulesReader.class.getResource("/META-INF/xsd/jqassistant-rules-1.0.xsd"));
        } catch (SAXException e) {
            throw new IllegalStateException("Cannot read rules schema.", e);
        }
        return schema;
    }


    private Map<String, ConstraintGroup> convert(List<JqassistantRules> rules) {
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
        Map<String, Concept> concepts = readConcepts(queryDefinitionTypes, conceptTypes);
        Map<String, Constraint> constraints = readConstraints(queryDefinitionTypes, conceptTypes, constraintTypes, concepts);
        Map<String, ConstraintGroup> constraintGroups = readConstraintGroups(constraintTypes, constraintGroupTypes, constraints);
        return constraintGroups;
    }

    private <T extends ReferenceableType> void cacheXmlTypes(List<T> list, Map<String, T> typeMap) {
        for (T t : list) {
            typeMap.put(t.getId(), t);
        }
    }

    private Map<String, Concept> readConcepts(Map<String, QueryDefinitionType> queryDefinitionTypes, Map<String, ConceptType> conceptTypes) {
        Map<String, Concept> concepts = new HashMap<String, Concept>();
        for (ConceptType conceptType : conceptTypes.values()) {
            Concept concept = getOrCreateConcept(conceptType.getId(), concepts);
            concept.setDescription(conceptType.getDescription());
            if (conceptType.getUseQueryDefinition() != null) {
                concept.setQuery(createQueryFromDefinition(conceptType.getUseQueryDefinition().getRefId(), conceptType.getParameter(), queryDefinitionTypes));
            } else {
                concept.setQuery(createQuery(conceptType.getCypher(), conceptType.getParameter()));
            }
            concept.setRequiredConcepts(getRequiredConcepts(conceptType.getRequiresConcept(), conceptTypes, concepts));
        }
        return concepts;
    }

    private Map<String, Constraint> readConstraints(Map<String, QueryDefinitionType> queryDefinitionTypes, Map<String, ConceptType> conceptTypes, Map<String, ConstraintType> constraintTypes, Map<String, Concept> concepts) {
        Map<String, Constraint> constraints = new HashMap<String, Constraint>();
        for (ConstraintType constraintType : constraintTypes.values()) {
            Constraint constraint = getOrCreateConstraint(constraintType.getId(), constraints);
            constraint.setDescription(constraintType.getDescription());
            if (constraintType.getUseQueryDefinition() != null) {
                constraint.setQuery(createQueryFromDefinition(constraintType.getUseQueryDefinition().getRefId(), constraintType.getParameter(), queryDefinitionTypes));
            } else {
                constraint.setQuery(createQuery(constraintType.getCypher(), constraintType.getParameter()));
            }
            constraint.setRequiredConcepts(getRequiredConcepts(constraintType.getRequiresConcept(), conceptTypes, concepts));
        }
        return constraints;
    }

    private Map<String, ConstraintGroup> readConstraintGroups(Map<String, ConstraintType> constraintTypes, Map<String, ConstraintGroupType> constraintGroupTypes, Map<String, Constraint> constraints) {
        Map<String, ConstraintGroup> constraintGroups = new HashMap<String, ConstraintGroup>();
        for (ConstraintGroupType constraintGroupType : constraintGroupTypes.values()) {
            ConstraintGroup constraintGroup = getOrCreateConstraintGroup(constraintGroupType.getId(), constraintGroups);
            for (ReferenceType referenceType : constraintGroupType.getIncludeConstraint()) {
                ConstraintType includedConstraintType = constraintTypes.get(referenceType.getRefId());
                if (includedConstraintType == null) {
                    throw new IllegalArgumentException("Cannot resolve included constraint: " + referenceType.getRefId());
                }
                constraintGroup.getConstraints().add(getOrCreateConstraint(referenceType.getRefId(), constraints));
            }
            for (ReferenceType referenceType : constraintGroupType.getIncludeConstraintGroup()) {
                ConstraintGroupType includedConstraintType = constraintGroupTypes.get(referenceType.getRefId());
                if (includedConstraintType == null) {
                    throw new IllegalArgumentException("Cannot resolve included constraint group: " + referenceType.getRefId());
                }
                constraintGroup.getConstraintGroups().add(getOrCreateConstraintGroup(referenceType.getRefId(), constraintGroups));
            }
        }
        return constraintGroups;
    }


    private Concept getOrCreateConcept(String id, Map<String, Concept> concepts) {
        Concept concept = concepts.get(id);
        if (concept == null) {
            concept = new Concept();
            concept.setId(id);
            concepts.put(id, concept);
        }
        return concept;
    }

    private Constraint getOrCreateConstraint(String id, Map<String, Constraint> constraints) {
        Constraint constraint = constraints.get(id);
        if (constraint == null) {
            constraint = new Constraint();
            constraint.setId(id);
            constraints.put(id, constraint);
        }
        return constraint;
    }

    private ConstraintGroup getOrCreateConstraintGroup(String id, Map<String, ConstraintGroup> constraintGroups) {
        ConstraintGroup constraintGroup = constraintGroups.get(id);
        if (constraintGroup == null) {
            constraintGroup = new ConstraintGroup();
            constraintGroup.setId(id);
            constraintGroups.put(id, constraintGroup);
        }
        return constraintGroup;
    }

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

    private Query createQuery(String cypher, List<ParameterType> parameterTypes) {
        Map<String, Object> defaultValues = Collections.emptyMap();
        return createQuery(cypher, parameterTypes, defaultValues);
    }

    private Query createQueryFromDefinition(String id, List<ParameterType> parameterTypes, Map<String, QueryDefinitionType> queryDefinitionTypes) {
        QueryDefinitionType queryDefinitionType = queryDefinitionTypes.get(id);
        if (id == null) {
            throw new IllegalArgumentException("Cannot resolve used query definition: " + id);
        }
        Map<String, Object> defaultValues = new HashMap<String, Object>();
        for (ParameterDefinitionType parameterDefinitionType : queryDefinitionType.getParameterDefinition()) {
            defaultValues.put(parameterDefinitionType.getName(), getValue(parameterDefinitionType.getType(), parameterDefinitionType.getDefault()));
        }
        return createQuery(queryDefinitionType.getCypher(), parameterTypes, defaultValues);
    }

    private Query createQuery(String cypher, List<ParameterType> parameterTypes, Map<String, Object> defaultValues) {
        Query query = new Query();
        query.setCypher(cypher);
        for (ParameterType parameterType : parameterTypes) {
            query.getParameters().put(parameterType.getName(), getValue(parameterType.getType(), parameterType.getValue()));
        }
        return query;
    }

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
