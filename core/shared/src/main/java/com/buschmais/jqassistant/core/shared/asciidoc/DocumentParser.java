package com.buschmais.jqassistant.core.shared.asciidoc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableMap;

/**
 * A parser for Asciidoc-Documents extracting rules from the document.
 */
public class DocumentParser {

    private static final String ROLE_CONCEPT = "concept";
    private static final String ROLE_CONSTRAINT = "constraint";
    private static final String ROLE_GROUP = "group";

    private static final String ID = "id";

    @Builder
    @Getter
    @ToString
    public static class Result {

        private Map<String, StructuralNode> concepts;

        private Map<String, StructuralNode> constraints;

        private Map<String, StructuralNode> groups;
    }

    public Result parse(Document document) {
        return parse(singletonList(document));
    }

    private Result parse(Collection<?> blocks) {
        Map<String, StructuralNode> concepts = new HashMap<>();
        Map<String, StructuralNode> constraints = new HashMap<>();
        Map<String, StructuralNode> groups = new HashMap<>();
        parse(blocks, concepts, constraints, groups);
        return Result.builder().concepts(unmodifiableMap(concepts)).constraints(unmodifiableMap(constraints)).groups(unmodifiableMap(groups)).build();
    }

    private void parse(Collection<?> blocks, Map<String, StructuralNode> conceptBlocks, Map<String, StructuralNode> constraintBlocks,
            Map<String, StructuralNode> groupBlocks) {
        if (blocks != null) {
            for (Object element : blocks) {
                if (element instanceof StructuralNode && !(element instanceof DescriptionListEntry)) {
                    StructuralNode block = (StructuralNode) element;
                    String role = block.getRole();
                    if (role != null) {
                        String id = (String) block.getAttribute(ID);
                        if (ROLE_CONCEPT.equalsIgnoreCase(role)) {
                            conceptBlocks.put(id, block);
                        } else if (ROLE_CONSTRAINT.equalsIgnoreCase(role)) {
                            constraintBlocks.put(id, block);
                        } else if (ROLE_GROUP.equalsIgnoreCase(role)) {
                            groupBlocks.put(id, block);
                        }
                    }
                    parse(block.getBlocks(), conceptBlocks, constraintBlocks, groupBlocks);
                } else if (element instanceof Collection<?>) {
                    parse((Collection<?>) element);
                }
            }
        }
    }
}
