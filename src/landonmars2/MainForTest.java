package landonmars2;

import graph.Node;

import java.util.ArrayList;
import java.util.List;

public class MainForTest {

    public static void main(String... args) {
        //test_OverGround();
        //test_CalculatePowerAndRotation();
        //test_ExploreNextSolution_Easy();
        //test_ExploreNextSolution();
        test_CalculateDistance();
    }

    private static List<Player.Coordinate> coordinates = new ArrayList<>();
    private static List<Player.Zone> zones = new ArrayList<>();
    private static Player.Zone landZone = new Player.Zone();
    private static Player.CalculatedData calculatedData = new Player.CalculatedData();

    private static void createCoordinates() {
        System.out.println("Method - createCoordinates");
        coordinates.add(new Player.Coordinate(0, 100));
        coordinates.add(new Player.Coordinate(1000, 500));
        coordinates.add(new Player.Coordinate(1500, 1500));
        coordinates.add(new Player.Coordinate(3000, 1000));
        coordinates.add(new Player.Coordinate(4000, 150));
        coordinates.add(new Player.Coordinate(5500, 150));
        coordinates.add(new Player.Coordinate(6999, 800));
    }

    public static void test_ConvertToZoneList() {
        System.out.println("Method - test_ConvertToZoneList");

        createCoordinates();
        Player.convertToZoneList(coordinates, zones);
    }

    public static void test_SearchBestLandZone() {
        System.out.println("Method - test_SearchBestLandZone");
        createCoordinates();
        Player.searchBestLandZone(landZone, coordinates);
    }

    public static void test_OverGround() {
        System.out.println("Method - test_OverGround");
        test_ConvertToZoneList();

        //System.out.println("[500,1000] is over ground : " + landonmars2.Player.Calculator.isOverGround(new landonmars2.Player.PreciseCoordinate(500, 1000), zones));
        //System.out.println("[2000,1000] is over ground : " + landonmars2.Player.Calculator.isOverGround(new landonmars2.Player.PreciseCoordinate(2000, 1000), zones));
        //System.out.println("[3500,1000] is over ground : " + landonmars2.Player.Calculator.isOverGround(new landonmars2.Player.PreciseCoordinate(3500, 1000), zones));
        //System.out.println("[5000,1000] is over ground : " + landonmars2.Player.Calculator.isOverGround(new landonmars2.Player.PreciseCoordinate(5000, 1000), zones));
    }

    public static void test_SplitVector() {
        System.out.println("Method - test_SplitVector");
        System.out.println("Vector1 : " + Player.Calculator.splitVector(10,3).toString());
        System.out.println("Vector2 : " + Player.Calculator.splitVector(45,3).toString());
        System.out.println("Vector3 : " + Player.Calculator.splitVector(90,3).toString());
    }

    public static void test_CalculateNext() {
        System.out.println("Method - test_CalculateNext");
        System.out.println(Player.Calculator.calculateNext(0, 10, 5));
        System.out.println(Player.Calculator.calculateNext(0, 5, 5));
        System.out.println(Player.Calculator.calculateNext(0, 3, 5));
    }

    public static void test_CalculateNextData() {
        System.out.println("Method - test_CalculateNextData");

        Player.Data myData = new Player.Data();
        myData.setCurrentPosition(new Player.PreciseCoordinate(3000, 2000));

        //No move
        myData.setHSpeed(0);
        myData.setVSpeed(0);
        myData.setActualRotation(0);
        myData.setAimedRotation(0);
        myData.setActualPower(0);
        myData.setAimedPower(0);
        Player.Calculator.calculateNextData(myData);
        System.out.println("");

        myData.setHSpeed(0);
        myData.setVSpeed(0);
        myData.setActualRotation(0);
        myData.setAimedRotation(-10);
        myData.setActualPower(0);
        myData.setAimedPower(3);
        /*while (landonmars2.Player.Calculator.isOverGround(myData.getCurrentPosition(), zones)) {
            myData = landonmars2.Player.Calculator.calculateNextData(myData);
        }*/
    }

    public static void test_EstimateLandingData() {
        createCoordinates();
        test_ConvertToZoneList();

        System.out.println("Method - test_EstimateLandingData");

        Player.Data myData = new Player.Data();
        myData.setCurrentPosition(new Player.PreciseCoordinate(2500, 2700));
        myData.setHSpeed(0d);
        myData.setVSpeed(0d);
        myData.setActualRotation(0);
        myData.setAimedRotation(-10);
        myData.setActualPower(0);
        myData.setAimedPower(3);

        Player.Data tmpData = new Player.Data().clone(myData);
        Player.Calculator.estimateLandingData(tmpData, calculatedData, zones);

        System.out.println(calculatedData.toString());
    }

    public static void test_CalculatePowerAndRotation() {
        test_SearchBestLandZone();
        test_EstimateLandingData();

        System.out.println("Method - test_CalculatePowerAndRotation");

        Player.Data myData = new Player.Data();
        myData.setCurrentPosition(new Player.PreciseCoordinate(2500, 2700));
        myData.setHSpeed(0d);
        myData.setVSpeed(0d);
        myData.setActualRotation(0);
        myData.setAimedRotation(0);
        myData.setActualPower(0);
        myData.setAimedPower(0);

        Player.Coordinate landingCoordinate = new Player.Coordinate(((landZone.getStart().getX() + landZone.getEnd().getX()) / 2), landZone.getStart().getY());

        /*while (landonmars2.Player.Calculator.isOverGround(myData.getCurrentPosition(), zones)) {
            landonmars2.Player.Calculator.calculatePowerAndRotation(landingCoordinate, myData);
        }*/


        System.out.println(myData.toString());
    }

    public static void test_ExploreNextSolution_Easy() {
        test_ConvertToZoneList();
        test_SearchBestLandZone();
        Player.PreciseCoordinate landingCoordinate = new Player.PreciseCoordinate(((landZone.getStart().getX() + landZone.getEnd().getX()) / 2), landZone.getStart().getY());
        System.err.println("Landing coordinate" + landingCoordinate.toString());

        Player.Data myData = new Player.Data();
        myData.setCurrentPosition(new Player.PreciseCoordinate(4750, 180));
        myData.setHSpeed(0d);
        myData.setVSpeed(0d);
        myData.setActualRotation(0);
        myData.setAimedRotation(0);
        myData.setActualPower(0);
        myData.setAimedPower(0);

        Player.Calculator.exploreNextSolution(myData, null, landingCoordinate, zones);
    }

    public static void test_ExploreNextSolution() {
        test_ConvertToZoneList();
        test_SearchBestLandZone();
        Player.PreciseCoordinate landingCoordinate = new Player.PreciseCoordinate(((landZone.getStart().getX() + landZone.getEnd().getX()) / 2), landZone.getStart().getY());
        System.err.println("Landing coordinate" + landingCoordinate.toString());

        Player.Data myData = new Player.Data();
        myData.setCurrentPosition(new Player.PreciseCoordinate(2500, 2700));
        myData.setHSpeed(0d);
        myData.setVSpeed(0d);
        myData.setActualRotation(0);
        myData.setAimedRotation(0);
        myData.setActualPower(0);
        myData.setAimedPower(0);

        Player.Calculator.exploreNextSolution(myData, null, landingCoordinate, zones);
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