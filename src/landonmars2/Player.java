package landonmars2;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/

class Player {

    private static int absRotationIncrement = 15;
    private static int absPowerIncrement = 1;
    private static int maxAbsRotation = 90;
    private static int minPower = 0;
    private static int maxPower = 4;
    private static int maxHSpeed = 20;
    private static int maxVSpeed = 40;
    private static double gravity = -3.711;

    private static DecimalFormat df = new DecimalFormat( "#0.0" );

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int surfaceN = in.nextInt(); // the number of points used to draw the surface of Mars.
        List<Coordinate> coordinateList = new ArrayList<>();
        for (int i = 0; i < surfaceN; i++) {
            int landX = in.nextInt(); // X coordinate of a surface point. (0 to 6999)
            int landY = in.nextInt(); // Y coordinate of a surface point. By linking all the points together in a sequential fashion, you form the surface of Mars.
            coordinateList.add(new Coordinate(landX, landY));
        }

        List<Zone> zones = new ArrayList<>();
        convertToZoneList(coordinateList, zones);

        Data myData = new Data();
        Zone zone = new Zone();
        if (!searchBestLandZone(zone, coordinateList)) {
            System.err.println("Bull shit ...");
        }

        System.err.println("Landing zone" + zone.toString());

        //Aimed the middle of the landing zone
        Coordinate landingCoordinate = new Coordinate(((zone.getStart().getX() + zone.getEnd().getX()) / 2), zone.getStart().getY());
        System.err.println("Landing coordinate" + landingCoordinate.toString());

        int step = 0;

        // game loop
        while (true) {
            int X = in.nextInt();
            int Y = in.nextInt();
            int hSpeed = in.nextInt(); // the horizontal speed (in m/s), can be negative.
            int vSpeed = in.nextInt(); // the vertical speed (in m/s), can be negative.
            int fuel = in.nextInt(); // the quantity of remaining fuel in liters.
            int rotate = in.nextInt(); // the rotation angle in degrees (-90 to 90).
            int power = in.nextInt(); // the thrust power (0 to 4).

            myData.setCurrentPosition(new PreciseCoordinate(X, Y));
            myData.setHSpeed(hSpeed);
            myData.setVSpeed(vSpeed);
            myData.setActualRotation(rotate);
            myData.setActualPower(power);

            System.err.println("Step " + step + " : " +  myData.toString() );

            CalculatedData calculatedData = new CalculatedData();
            Calculator.estimateLandingData(myData, calculatedData, zones);
            System.err.println("Step " + step + " : " +  calculatedData.toString());

            Calculator.calculatePowerAndRotation(landingCoordinate, myData);

            if (calculatedData.getEstimatedTimeToLand() > Calculator.calculateTimeToRotateToLand(rotate) ) {
                System.out.println(myData.getAimedRotation() + " " + myData.getAimedPower());
            } else {
                //Start landing
                System.out.println("0 " + maxPower);
            }

            step++;
        }
    }

    public static void convertToZoneList(List<Coordinate> list, List<Zone> zoneList) {
        Zone previousZone = null;
        for(Coordinate coordinate : list) {
            if (previousZone != null) {
                previousZone.setEnd(new Coordinate().clone(coordinate));
                zoneList.add(previousZone);
            }
            previousZone = new Zone(new Coordinate().clone(coordinate), null);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (Zone z : zoneList) {
            stringBuilder.append(z.toString());
        }
        System.err.println(stringBuilder.toString());
    }

    public static boolean searchBestLandZone(Zone zone, List<Coordinate> list) {
        //TODO looking for the nearest and biggest flat zone

        //Add flat zones
        Map<Coordinate, Coordinate> flatZones = new HashMap<>();
        Integer currentY = null;
        Coordinate start = null;
        for(Coordinate coordinate : list) {
            if (currentY == null) {
                currentY = coordinate.getY();
                start = coordinate;
            } else {
                if (currentY == coordinate.getY()) {
                    //it's a flat zone
                    flatZones.put(start, coordinate);
                } else {
                    start = coordinate;
                    currentY = coordinate.getY();
                }
            }
        }

        //Select the biggest flat zone
        int biggestSizeZone = 0;
        for(Map.Entry<Coordinate, Coordinate> entry : flatZones.entrySet()) {
            int sizeZone = entry.getValue().getX() - entry.getKey().getX();
            if (sizeZone > biggestSizeZone) {
                biggestSizeZone = sizeZone;
                zone.setStart(entry.getKey());
                zone.setEnd(entry.getValue());
            }
        }

        System.err.println("Landing zone : " + zone.toString());

        return biggestSizeZone != 0;
    }

    public static class Calculator {
        public static void estimateLandingData(Data data, CalculatedData calculatedData, List<Zone> zones) {
            Data nextData = calculateNextData(data);
            if (isOverGround(nextData.getCurrentPosition(), zones)) {

                //System.err.println(nextData.toString());

                calculatedData.setEstimatedTimeToLand(calculatedData.getEstimatedTimeToLand() + 1);
                calculatedData.setEstimatedCoordinateToLand(nextData.getCurrentPosition());
                estimateLandingData(nextData, calculatedData, zones);
            } else {
                //Calculate exact landing coordinate
                //TODO
            }
        }

        public static Data calculateNextData(Data data) {
            Data nextData = new Data();

            //Aimed data
            nextData.setAimedPower(data.getAimedPower());
            nextData.setAimedRotation(data.getAimedRotation());

            //Next rotation
            nextData.setActualRotation(calculateNext(data.getActualRotation(), data.getAimedRotation(), absRotationIncrement));

            //Next power
            nextData.setActualPower(calculateNext(data.getActualPower(), data.getAimedPower(), absPowerIncrement));

            //Power
            Vector nextPowerVector = splitVector(nextData.getActualRotation(), (double)nextData.getActualPower());

            //Next position
            PreciseCoordinate currentPosition = data.getCurrentPosition();
            double nextX = currentPosition.getX() + data.getHSpeed();
            double nextY = currentPosition.getY() + data.getVSpeed();
            PreciseCoordinate nextPosition = new PreciseCoordinate(nextX, nextY);
            nextData.setCurrentPosition(nextPosition);

            //Next speed
            double nextVSpeed = data.getVSpeed() + gravity + nextPowerVector.getY();
            nextData.setVSpeed(nextVSpeed);
            double nextHSpeed = data.getHSpeed() + nextPowerVector.getX();
            nextData.setHSpeed(nextHSpeed);

            return nextData;
        }

        /**
         * Use to calculate the next value of an incremented data
         */
        public static int calculateNext(int data, int aimedData, int increment) {
            if (data > aimedData) {
                if (data > (aimedData + increment)) {
                    return data - increment;
                } else {
                    return aimedData;
                }
            } else if (data < aimedData) {
                if (data < (aimedData - increment)) {
                    return data + increment;
                } else {
                    return aimedData;
                }
            }
            return data;
        }

        public static Vector splitVector(int angle, double length) {
            double x = Math.sin(Math.toRadians(Math.abs(angle))) * length;
            double y = Math.cos(Math.toRadians(Math.abs(angle))) * length;
            return new Vector(x, y);
        }

        static boolean isOverGround(PreciseCoordinate c, List<Zone> zones) {
            for (Zone zone : zones) {
                if (c.getX() >= zone.getStart().getX() && c.getX() <= zone.getEnd().getX()) {
                    if (zone.getStart().getY() == zone.getEnd().getY()) {
                        //Flat zone
                        return c.getY() > zone.getStart().getY();
                    } else {
                        double coefficient = (c.getX() - zone.getStart().getX()) / (double)(zone.getEnd().getX() - zone.getStart().getX());
                        int altitudeDifference = zone.getStart().getY() - zone.getEnd().getY();
                        double altitude = (double)zone.getStart().getY() -  coefficient * (double) altitudeDifference;
                        return c.getY() > altitude;
                    }
                }
            }
            //Not possible
            return false;
        }

        static int calculateTimeToRotateToLand(int angle) {
            return (int) Math.ceil(Math.abs(angle) / absRotationIncrement);
        }

        static void calculatePowerAndRotation(Coordinate coordinate, Data data) {
            int incrementAngle = (maxAbsRotation - Math.abs(data.getActualRotation())) / 3;
            if (Math.abs(data.getHSpeed()) > maxHSpeed) {
                if (data.getHSpeed() < 0) {
                    //Going to the left
                    if (data.getActualRotation() > 0) {
                        data.setAimedRotation(0);
                    } else {
                        data.setAimedRotation(data.getAimedRotation() + incrementAngle);
                    }
                } else if (data.getHSpeed() > 0) {
                    //Going to the right
                    if (data.getActualRotation() < 0) {
                        data.setAimedRotation(0);
                    } else {
                        data.setAimedRotation(data.getAimedRotation() + incrementAngle);
                    }
                }
            } else {
                if (data.getCurrentPosition().getX() > coordinate.getX()) {
                    data.setAimedRotation(data.getActualRotation() + incrementAngle);
                } else if (data.getCurrentPosition().getX() < coordinate.getX()) {
                    data.setAimedRotation(data.getActualRotation() - incrementAngle);
                }
            }

            if (Math.abs(data.getVSpeed()) > maxVSpeed) {
                if (data.getActualPower() < maxPower) {
                    data.setAimedPower(data.getActualPower() + 1);
                }
            }
        }
    }

    public static class Coordinate {
        private int x;
        private int y;

        Coordinate() {
        }

        Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        Coordinate clone(Coordinate c) {
            this.x = c.getX();
            this.y = c.getY();
            return this;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        @Override
        public String toString() {
            return "[" + x + "," + y + "]";
        }
    }

    public static class Zone {
        private Coordinate start;
        private Coordinate end;

        Zone() {
            start = new Coordinate(-1, -1);
            end = new Coordinate(-1, -1);
        }

        Zone(Coordinate x1, Coordinate x2) {
            this.start = x1;
            this.end = x2;
        }

        public Coordinate getStart() {
            return start;
        }

        public Coordinate getEnd() {
            return end;
        }

        void setStart(Coordinate x) {
            this.start = x;
        }

        void setEnd(Coordinate x) {
            this.end = x;
        }

        @Override
        public String toString() {
            return "{" + start.toString() + "-" + end.toString() + "}";
        }
    }

    public static class Data {
        private int actualRotation;
        private int aimedRotation;
        private int actualPower;
        private int aimedPower;
        private PreciseCoordinate currentPosition;
        private double vSpeed;
        private double hSpeed;

        Data() {
        }

        @Override
        public String toString() {
            return "[" + "Actual rotation : " + actualRotation + " ; " +
                    "Aimed rotation : " + aimedRotation + " ; " +
                    "Actual power : " + actualPower + " ; " +
                    "Aimed power : " + aimedPower + " ; " +
                    "Current position : " + currentPosition.toString() + " ; " +
                    "V speed : " + Math.round(vSpeed) + " ; " +
                    "H speed : " + Math.round(hSpeed) +
                    "]";
        }

        public void clone(Data d) {
            this.actualRotation = d.getActualRotation();
            this.aimedRotation = d.getAimedRotation();
            this.actualPower = d.getActualPower();
            this.aimedPower = d.getAimedPower();
            this.currentPosition = (new PreciseCoordinate()).clone(d.getCurrentPosition());
        }

        public int getActualRotation() {
            return actualRotation;
        }

        public int getAimedRotation() {
            return aimedRotation;
        }

        public int getActualPower() {
            return actualPower;
        }

        public int getAimedPower() {
            return aimedPower;
        }

        public PreciseCoordinate getCurrentPosition() {
            return currentPosition;
        }

        public void setActualRotation(int r) {
            this.actualRotation = r;
        }

        public void setAimedRotation(int r) {
            this.aimedRotation = r;
        }

        public void setActualPower(int actualPower) {
            this.actualPower = actualPower;
        }

        public void setAimedPower(int aimedPower) {
            this.aimedPower = aimedPower;
        }

        public void setCurrentPosition(PreciseCoordinate c) {
            this.currentPosition = c;
        }

        public double getVSpeed() {
            return vSpeed;
        }

        public void setVSpeed(double vSpeed) {
            this.vSpeed = vSpeed;
        }

        public double getHSpeed() {
            return hSpeed;
        }

        public void setHSpeed(double hSpeed) {
            this.hSpeed = hSpeed;
        }
    }

    public static class CalculatedData {
        private int estimatedTimeToLand;
        private PreciseCoordinate estimatedCoordinateToLand;

        CalculatedData() {
        }

        @Override
        public String toString() {
            return "[" + "Estimated time to land : " + estimatedTimeToLand + " ; " +
                    "Estimated coordinate to land : " + estimatedCoordinateToLand +
                    "]";
        }

        public int getEstimatedTimeToLand() {
            return estimatedTimeToLand;
        }

        public PreciseCoordinate getEstimatedCoordinateToLand() {
            return estimatedCoordinateToLand;
        }

        public void setEstimatedTimeToLand(int t) {
            this.estimatedTimeToLand = t;
        }

        public void setEstimatedCoordinateToLand(PreciseCoordinate c) {
            this.estimatedCoordinateToLand = c;
        }
    }

    public static class Vector {
        private double x;
        private double y;

        Vector(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "[" + x + "," + y + "]";
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    public static class PreciseCoordinate {
        private double x;
        private double y;

        PreciseCoordinate() {
        }

        PreciseCoordinate(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public PreciseCoordinate clone(PreciseCoordinate coordinate) {
            return new PreciseCoordinate(coordinate.getX(), coordinate.getY());
        }

        @Override
        public String toString() {
            return "[" + df.format(x) + ";" + df.format(y) + "]";
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }
}