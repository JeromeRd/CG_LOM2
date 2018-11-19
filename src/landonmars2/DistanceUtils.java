package landonmars2;

import graph.Node;

public class DistanceUtils {

    public static double calculateDistance(Node n1, Node n2) {
        double a = Math.abs(n1.getX() - n2.getX());
        double b = Math.abs(n1.getY() - n2.getY());
        return Math.sqrt(a * a + b * b);
    }

    public static int horizontalDistance(Node n1, Node n2) {
        return Math.abs(n1.getX() - n2.getX());
    }
}
