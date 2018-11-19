package graph;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

public class MainForGraph {

    public static void main(String[] args) {
        //shortTest_Depth();
        TimeService.start();

        shortTest_Width(100, 100);

        System.out.println("Elapsed time : "+TimeService.getElapsedTime()+"ms");
    }

    private static void shortTest_Depth(int xGoal, int yGoal) {
        GraphBuilder graphBuilder = new GraphBuilder(xGoal, yGoal, 0, 0);
        graphBuilder.fillGraphInDepth(10);
        GraphBrowser graphBrowser = new GraphBrowser(graphBuilder.getGraph());
        List<Node> bestWay = graphBrowser.getBestWay();
        display(bestWay);
    }

    private static void shortTest_Width(int xGoal, int yGoal) {
        GraphBuilder graphBuilder = new GraphBuilder(xGoal, yGoal, 0, 0);
        graphBuilder.fillGraphInWidth(100, 100);
        GraphBrowser graphBrowser = new GraphBrowser(graphBuilder.getGraph());
        List<Node> bestWay = graphBrowser.getBestWay();
        display(bestWay);
    }

    private static void display(List<Node> bestWay) {
        for (Node node : bestWay) {
            //Logger.log(node);
            //System.out.println(node);
        }
    }
}
