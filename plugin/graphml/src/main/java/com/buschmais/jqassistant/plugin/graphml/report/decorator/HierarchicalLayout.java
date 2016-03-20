package com.buschmais.jqassistant.plugin.graphml.report.decorator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.PipeBase;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.layout.Layout;

import java.util.*;

/**
 * Created by cdutz on 02.03.2016.
 */
public class HierarchicalLayout extends PipeBase implements Layout {

    public static final String TOPSORT_LEVE_FLAG = "top-sort-level";

    protected final Graph internalGraph;

    protected int xGap;
    protected int yGap;

    protected Point3 bottomLeftFront;
    protected Point3 topRightBack;

    protected boolean structureInvalidated;
    protected Map<Integer, List<Node>> levels;
    protected double currentStabilization = 0d;

    public HierarchicalLayout(int xGap, int yGap) {
        internalGraph = new AdjacencyListGraph("hierarchical_layout-intern");
        bottomLeftFront = new Point3();
        topRightBack = new Point3(0, 0, 0);
        structureInvalidated = false;

        this.xGap = xGap;
        this.yGap = yGap;
    }

    @Override
    public String getLayoutAlgorithmName() {
        return "Hierarchical";
    }

    @Override
    public void compute() {
        // Calculate some dummy positions for each node.
        if(structureInvalidated) {
            computePositions();
            structureInvalidated = false;
            publishPositions();
        }

        // Each incoming and outgoing edge applies force to the
        // the nodes it's attached to. The following sorting will
        // check for each node of a layer, if swapping it's position
        // with another node of the same layer will reduce the force
        // on the current layer and eventually continue swapping till
        // a minimum force is reached.
        boolean changed = false;
        if(levels != null) {
            for (Integer levelNumber : levels.keySet()) {
                List<Node> level = levels.get(levelNumber);
                changed |= sortNodes(level);
            }
        }

        // If changes were applied, update the nodes positions.
        if(changed) {
            currentStabilization = 0d;
            publishPositions();
        } else {
            currentStabilization = 1d;
        }
    }

    protected void computePositions() {
        // Create a topological sort on the graph.
        // This adds a "top-sort-level" attribute to
        // each node which tells it which level it
        // should be located on.
        TopologicalSort topSort = new TopologicalSort(TOPSORT_LEVE_FLAG);
        topSort.init(internalGraph);
        topSort.compute();

        // Create layers containing the nodes of the same level
        levels = new TreeMap<>();
        for(Node node : internalGraph.getNodeSet()) {
            Integer level = node.getAttribute(TOPSORT_LEVE_FLAG);
            if(!levels.containsKey(level)) {
                levels.put(level, new ArrayList<Node>());
            }
            levels.get(level).add(node);
        }

        // Search for the highest layer and for the one with the biggest
        // width (this decides the graph width and height).
        int maxWidth = 0;
        int maxLevel = 0;
        for(Integer level : levels.keySet()) {
            int levelWidth = levels.get(level).size();
            if(levelWidth > maxWidth) {
                maxWidth = levelWidth;
            }
            if(level > maxLevel) {
                maxLevel = level;
            }
        }

        // Set the bounds of the graph
        int width = (maxLevel + 1) * xGap;
        bottomLeftFront.setX(width);
        bottomLeftFront.setY(maxLevel * yGap);

        // Set the X and Y positions of all nodes.
        for(Map.Entry<Integer, List<Node>> level : levels.entrySet()) {
            int yPos = level.getKey() * yGap;
            int levelBoxSize = width / level.getValue().size();
            int xPos = levelBoxSize / 2;
            for(Node node : level.getValue()) {
                node.addAttribute("x", xPos);
                node.addAttribute("y", yPos);
                xPos += levelBoxSize;
            }
        }
    }

    protected void publishPositions() {
        for (Node n : internalGraph) {
            sendNodeAttributeChanged(sourceId, n.getId(), "xyz", null,
                    new double[] { n.getNumber("x"), n.getNumber("y"), 0 });
        }
    }

    @Override
    public void clear() {
        internalGraph.clear();
    }

    @Override
    public void nodeAdded(String sourceId, long timeId, String nodeId) {
        internalGraph.addNode(nodeId);
        invalidateStructure();
    }

    @Override
    public void nodeRemoved(String sourceId, long timeId, String nodeId) {
        internalGraph.removeNode(nodeId);
        invalidateStructure();
    }

    @Override
    public void edgeAdded(String sourceId, long timeId, String edgeId, String fromNodeId, String toNodeId, boolean directed) {
        internalGraph.addEdge(edgeId, fromNodeId, toNodeId, directed);
        invalidateStructure();
    }

    @Override
    public void edgeRemoved(String sourceId, long timeId, String edgeId) {
        internalGraph.removeEdge(edgeId);
        invalidateStructure();
    }

    @Override
    public void graphCleared(String sourceId, long timeId) {
        internalGraph.clear();
        invalidateStructure();
    }

    @Override
    public int getNodeMovedCount() {
        return 0;
    }

    @Override
    public double getStabilization() {
        return currentStabilization;
    }

    @Override
    public double getStabilizationLimit() {
        return 1;
    }

    @Override
    public Point3 getLowPoint() {
        return bottomLeftFront;
    }

    @Override
    public Point3 getHiPoint() {
        return topRightBack;
    }

    @Override
    public int getSteps() {
        return 0;
    }

    @Override
    public long getLastStepTime() {
        // TODO: Implement
        return 0;
    }

    @Override
    public double getQuality() {
        return 0;
    }

    @Override
    public double getForce() {
        return 0;
    }

    @Override
    public void setForce(double value) {
    }

    @Override
    public void setStabilizationLimit(double value) {
    }

    @Override
    public void setQuality(double qualityLevel) {
    }

    @Override
    public void setSendNodeInfos(boolean send) {
    }

    @Override
    public void shake() {
    }

    @Override
    public void moveNode(String id, double x, double y, double z) {
    }

    @Override
    public void freezeNode(String id, boolean frozen) {
    }

    protected void invalidateStructure() {
        structureInvalidated = true;
    }

    private double calculateAbsoluteForce(Node node, int nodePos) {
        double force = 0d;
        for(Edge edge : node.getEnteringEdgeSet()) {
            Node otherNode = edge.getSourceNode();
            int otherPos = otherNode.getAttribute("x");
            force += Math.abs(nodePos - otherPos);
        }
        for(Edge edge : node.getLeavingEdgeSet()) {
            Node otherNode = edge.getTargetNode();
            int otherPos = otherNode.getAttribute("x");
            force += Math.abs(nodePos - otherPos);
        }
        return force;
    }

    private double calculateLayerAbsoluteForce(Map<Node, Integer> layerNodePositions) {
        double totalAbsoluteForce = 0;
        for(Node node : layerNodePositions.keySet()) {
            int nodeXPosition = layerNodePositions.get(node);
            totalAbsoluteForce += calculateAbsoluteForce(node, nodeXPosition);
        }
        return totalAbsoluteForce;
    }

    private boolean sortNodes(List<Node> layerNodes) {
        boolean changed = false;
        // Build a map of all nodes of the current layer and their current position.
        Map<Node, Integer> scenario = new HashMap<>(layerNodes.size());
        for(Node node : layerNodes) {
            scenario.put(node, (Integer) node.getAttribute("x"));
        }

        // For each node in the layer check if swapping it with another node
        // reduces the overall layer force, if it does, swap the nodes.

        for(Node node : layerNodes) {
            int nodePosition = scenario.get(node);
            Node bestSwapCandidate = null;
            double bestOptimizationLayerForce = calculateLayerAbsoluteForce(scenario);
            for(Node otherNode : layerNodes) {
                if (otherNode != node) {
                    int otherNodeXPosition = scenario.get(otherNode);

                    // Swap the positions.
                    scenario.put(node, otherNodeXPosition);
                    scenario.put(otherNode, nodePosition);

                    // If swapping would reduce the force on the layer, remember this swap.
                    double currentLayerForce = calculateLayerAbsoluteForce(scenario);
                    if (currentLayerForce < bestOptimizationLayerForce) {
                        bestSwapCandidate = otherNode;
                        bestOptimizationLayerForce = currentLayerForce;
                    }

                    // Change the positions back.
                    scenario.put(node, nodePosition);
                    scenario.put(otherNode, otherNodeXPosition);
                }
            }
            // If a better position was found, execute the swap.
            if (bestSwapCandidate != null) {
                // Permanently swap the positions.
                scenario.put(node, scenario.get(bestSwapCandidate));
                scenario.put(bestSwapCandidate, nodePosition);
            }
        }

        // Update the x-positions of each node.
        for(Node node : layerNodes) {
            if(!node.getAttribute("x").equals(scenario.get(node))) {
                node.setAttribute("x", scenario.get(node));
                changed = true;
            }
        }

        return changed;
    }

}
