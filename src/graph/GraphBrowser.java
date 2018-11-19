package graph;

import java.util.ArrayList;
import java.util.List;

public class GraphBrowser {
    private Graph graph;

    public GraphBrowser(Graph graph) {
        this.graph = graph;
    }

    public List<Node> getBestWay() {
        Logger.jumpLine();
        Logger.log("Get the best way");
        List<Node> result = new ArrayList<>();
        Node node = graph.getBest();
        while (node != null) {
            result.add(node);
            node = node.getParent();
        }
        return result;
    }
}
