package landonmars2;

import graph.Node;

public class MainForTest2 {

    public static void main(String... args) {

        test_CalculateDistance();
    }

    public static void test_CalculateDistance() {
        Node n1 = new Node();
        n1.setX(0);
        n1.setY(0);

        Node n2 = new Node();
        n2.setX(4);
        n2.setY(0);

        double d = DistanceUtils.calculateDistance(n1, n2);

        n1.setX(1);
        n1.setY(2);
        n2.setX(4);
        n2.setY(-2);

        d = DistanceUtils.calculateDistance(n1, n2);
    }
}