package com.buschmais.jqassistant.plugin.graphml.report.decorator;

import org.graphstream.algorithm.Algorithm;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by cdutz on 02.03.2016.
 */
public class TopologicalSort implements Algorithm {

    private String levelAttributeName;
    private Graph graph;

    public TopologicalSort(String levelAttributeName) {
        this.levelAttributeName = levelAttributeName;
    }

    public String getLevelAttributeName() {
        return levelAttributeName;
    }

    @Override
    public void init(Graph graph) {
        this.graph = graph;
    }

    @Override
    public void compute() {
        Map<Node, Integer> levels = new HashMap<>(graph.getNodeCount());

        // First we need to find the roots nodes and start
        // sorting the nodes there.
        Stack<Node> path = new Stack<>();
        for(Node node : graph.getNodeSet()) {
            if(node.getInDegree() == 0) {
                levels.put(node, 0);
                path.clear();
                path.push(node);
                sortSubtree(node, levels, path);
            }
        }

        // If no root element was found, the map should be empty.
        // But this is bad for a topological sort.
        if(levels.size() == 0) {
            throw new RuntimeException("Could not find root-element");
        }

        // Add the level attributes to the nodes after
        // finishing the top-sort.
        for(Node node : graph.getNodeSet()) {
            int level = levels.get(node);
            node.addAttribute(levelAttributeName, level);
        }
    }

    protected void sortSubtree(Node curNode, Map<Node, Integer> levels, Stack<Node> path) {
        int curLevel = levels.get(curNode);
        for(Edge outgoingEdge : curNode.getLeavingEdgeSet()) {
            Node targetNode = outgoingEdge.getTargetNode();

            // If the graph contains a cycle, abort.
            if(path.contains(targetNode)) {
                throw new RuntimeException("This graph contains a cycle.");
            }

            // If the node was already located in a level, we have to
            // check if we have to move the node up
            if(levels.containsKey(targetNode)) {
                int targetLevel = levels.get(targetNode);
                // If the node was currently located at the same level or below
                // the current node ... bump up the entire tree.
                if(targetLevel <= curLevel) {
                    // Update the level of the subtree.
                    levels.put(targetNode, curLevel + 1);
                    path.push(targetNode);
                    sortSubtree(targetNode, levels, path);
                    path.pop();
                }
                // If the node is already located "above" the current
                // node, we don't need to do anything.
            } else {
                levels.put(targetNode, curLevel + 1);
                path.push(targetNode);
                sortSubtree(targetNode, levels, path);
                path.pop();
            }
        }
    }

}
