package graph;

import java.util.ArrayList;
import java.util.List;

public class Node {
    int x, y;
    double distance;
    double score;
    Node parent;
    List<Node> children;

    public Node() {
        children = new ArrayList<>();
    }

    public Node(int x, int y) {
        this();
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Node[x=").append(x).append(", y=").append(y)
                .append(", distance=").append(distance).append(", score=").append(score)
                .append("]");
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Node))return false;
        Node node = (Node)o;
        if (node.getX() == x && node.getY() == y) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + x;
        result = 31 * result + y;
        return result;
    }
}
