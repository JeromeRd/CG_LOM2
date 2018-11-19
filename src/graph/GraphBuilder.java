package graph;

import landonmars2.DistanceUtils;

import java.util.*;

public class GraphBuilder {
    private Node goal;
    private Graph graph;
    private List<Node> exploredNodes;

    public GraphBuilder(int xGoal, int yGoal, int xRoot, int yRoot) {
        goal = new  Node(xGoal, yGoal);
        Node root = new Node(xRoot, yRoot);
        root.setDistance(DistanceUtils.calculateDistance(root, goal));
        root.setScore(DistanceUtils.calculateDistance(root, goal));
        graph = new Graph(root);
        exploredNodes = new ArrayList<>();
    }

    public void fillGraphInDepth(int maxDepth) {
        Node root = graph.getRoot();
        if (root.equals(goal)) {
            onGoalReached(root, null);
        }
        fillGraphInDepth(root, maxDepth, 0);
    }

    public void fillGraphInWidth(int maxDepth, int maxTime) {
        Node root = graph.getRoot();
        if (root.equals(goal)) {
            onGoalReached(root);
        }
        fillGraphInWidth(Collections.singletonList(root), maxDepth, 0, maxTime);
    }

    private void onGoalReached(Node node, Node parent) {
        Logger.log("Goal has been reached");

        if (parent != null) {
            parent.getChildren().add(node);
            node.setParent(parent);
        }
        node.setScore(parent != null ? parent.getScore()-100000 : -100000);
        if (graph.getBest() == null || graph.getBest().getScore() > node.getScore()) {
            graph.setBest(node);
        }
    }

    private void fillGraphInDepth(Node parent, int maxDepth, int depth) {
        Logger.jumpLine();
        Logger.log("Fill graph for parent["+parent+"] with depth["+depth+"]");
        Logger.jumpLine();

        List<Node> possibilities = getPossibilities(parent);
        Iterator<Node> iterator = possibilities.iterator();
        while (depth <= maxDepth && iterator.hasNext()) {
            Node possibility = iterator.next();
            double distance = DistanceUtils.calculateDistance(possibility, goal);
            if (possibility.equals(goal)) {
                onGoalReached(possibility, parent);
                break;
            } else {
                possibility.setScore(parent.getScore()+distance);
            }
            possibility.setDistance(distance);

            int possibilityHDistance = DistanceUtils.horizontalDistance(possibility, goal);
            int parentHDistance = DistanceUtils.horizontalDistance(parent, goal);
            if (possibilityHDistance <= parentHDistance) {
                parent.getChildren().add(possibility);
                possibility.setParent(parent);
                if (depth == maxDepth) {
                    if (graph.getBest() == null || graph.getBest().getScore() > possibility.getScore()) {
                        graph.setBest(possibility);
                    }
                }
                fillGraphInDepth(possibility, maxDepth, depth + 1);
            } else {
                Logger.log("Don't fill graph for "+possibility+" because its hDistance["+possibilityHDistance+"] > parent hDistance["+parentHDistance+"]");
            }
        }
    }

    private void onGoalReached(Node node) {
        Logger.log("Goal has been reached");

        node.setScore(node.getScore()-100000);
        checkBestScore(node);
    }

    private void checkBestScore(Node node) {
        if (graph.getBest() == null || graph.getBest().getScore() > node.getScore()) {
            graph.setBest(node);
        }
    }

    private void fillGraphInWidth(List<Node> nodes, int maxDepth, int depth, int maxTime) {
        if (depth == maxDepth) {
            Logger.log("Max depth is reached");
            return;
        }

        List<Node> allPossibilities = new ArrayList<>();
        for (Node node : nodes) {
            if (exploredNodes.contains(node)) {
                Logger.log("Ignore already explored node ["+node+"]");
                continue;
            }

            Logger.jumpLine();
            Logger.log("Fill graph for node["+node+"] with depth["+depth+"]");
            Logger.jumpLine();

            double distance = DistanceUtils.calculateDistance(node, goal);
            node.setDistance(distance);
            if (node.equals(goal)) {
                onGoalReached(node);
                return;
            } else {
                node.setScore(node.getScore() + distance);
            }

            if (conditionsAreRespected(node, maxTime)) {
                List<Node> possibilities = getPossibilities(node);
                for (Node possibility : possibilities) {
                    possibility.setParent(node);
                    node.getChildren().add(possibility);
                }
                allPossibilities.addAll(possibilities);
                checkBestScore(node);
                exploredNodes.add(node);
            }
        }
        fillGraphInWidth(allPossibilities, maxDepth, depth + 1, maxTime);
    }

    private boolean conditionsAreRespected(Node node, int maxTime) {
        /*int nodeHDistance = DistanceUtils.horizontalDistance(node, goal);
        int parentHDistance = Integer.MAX_VALUE;
        if (node.getParent() != null) {
            parentHDistance = DistanceUtils.horizontalDistance(node.getParent(), goal);
        }

        if (nodeHDistance <= parentHDistance) {
            result = true;
        } else {
            Logger.log("Don't fill graph for " + node + " because its hDistance[" + nodeHDistance + "] > parent hDistance[" + parentHDistance + "]");
        }*/
        long elapsedTime = TimeService.getElapsedTime();
        if (elapsedTime > maxTime) {
            Logger.log("Don't fill graph for " + node + " because time is over [" + elapsedTime + "] > [" + maxTime + "]");
            return false;
        }

        double parentDistance = Integer.MAX_VALUE;
        if (node.getParent() != null) {
            parentDistance = node.getParent().getDistance();
        }
        if (node.getDistance() > parentDistance) {
            Logger.log("Don't fill graph for " + node + " because its hDistance[" + node.getDistance() + "] > parent hDistance[" + parentDistance + "]");
            return false;
        }
        return true;
    }

    public Graph getGraph() {
        return graph;
    }

    private List<Node> getPossibilities(Node node) {
        Logger.log("Possibilities for node"+node.toString());

        List<Node> result = new ArrayList<>();
        result.add(new Node(node.getX()+1, node.getY()));
        result.add(new Node(node.getX()+1, node.getY()+1));
        result.add(new Node(node.getX(), node.getY()+1));
        result.add(new Node(node.getX()-1, node.getY()+1));
        result.add(new Node(node.getX()-1, node.getY()));
        result.add(new Node(node.getX()-1, node.getY()-1));
        result.add(new Node(node.getX(), node.getY()-1));
        result.add(new Node(node.getX()+1, node.getY()-1));

        Logger.jumpLine();

        return result;
    }
}
