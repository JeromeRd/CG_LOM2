package graph;

public class Graph {
    private Node root;

    private Node best;

    public Graph(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public Node getBest() {
        return best;
    }

    public void setBest(Node best) {
        this.best = best;
    }
}
