import java.text.DecimalFormat;
import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/

class Player {

    private static int absRotationIncrementMax = 15;
    private static int absRotationIncrementUnit = 1;
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
        List<Coordinate> coordList = new ArrayList<>();
        for (int i = 0; i < surfaceN; i++) {
            int landX = in.nextInt(); // X coordinate of a surface point. (0 to 6999)
            int landY = in.nextInt(); // Y coordinate of a surface point. By linking all the points together in a sequential fashion, you form the surface of Mars.
            coordList.add(new Coordinate(landX, landY));
        }

        List<Zone> zones = new ArrayList<>();
        convertToZoneList(coordList, zones);

        Data myData = new Data();
        Zone zone = new Zone();
        if (!searchBestLandZone(zone, coordList)) {
            System.err.println("Bull shit ...");
        }

        System.err.println("Landing zone" + zone.toString());

        //Aimed the middle of the landing zone
        Coordinate landingCoordinate = new Coordinate(((zone.getStart().getX() + zone.getEnd().getX()) / 2), zone.getStart().getY());
        System.err.println("Landing coordinate" + landingCoordinate.toString());

        Calculator calculator = new Calculator();

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
            calculator.estimateLandingData(myData, calculatedData, zones);
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
                currentY = Integer.valueOf(coordinate.getY());
                start = coordinate;
            } else {
                if (currentY.intValue() == coordinate.getY()) {
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

        if (biggestSizeZone == 0) return false;
        else return true;
    }

    public static class Calculator {


        public static void estimateLandingData(Data data, CalculatedData calculatedData, List<Zone> zones) {
            Data nextData = calculateNextData(data);
            if (Position.OVER.equals(getPositionRelativeToGround(nextData.getCurrentPosition(), zones))) {
                if (calculatedData.getEstimatedTimeToLand() > Calculator.calculateTimeToRotateToLand(data.getActualRotation()) ) {
                   nextData.setAimedRotation(0);
                }

                calculatedData.setEstimatedTimeToLand(calculatedData.getEstimatedTimeToLand() + 1);
                calculatedData.setEstimatedCoordinateToLand(nextData.getCurrentPosition());
                calculatedData.setLandingVSpeed(nextData.getVSpeed());
                calculatedData.setLandingHSpeed(nextData.getHSpeed());
                estimateLandingData(nextData, calculatedData, zones);
            } else {


                calculatedData.setEstimatedTimeToLand(calculatedData.getEstimatedTimeToLand() + 1);
                calculatedData.setEstimatedCoordinateToLand(calculateExactLandingCoordinates());
                calculatedData.setLandingVSpeed(nextData.getVSpeed());
                calculatedData.setLandingHSpeed(nextData.getHSpeed());
                estimateLandingData(nextData, calculatedData, zones);

                //Return exact landing coordinate (thanks Thales, Pythagore and co)
                double ac = data.getCurrentPosition().getY() - nextData.getCurrentPosition().getY();
                double ae = ;
                getAltitude(data.getCurrentPosition().getX(), getZone(data.getCurrentPosition().getX(), zones));
            }
        }

        public static Data calculateNextData(Data data) {
            Data nextData = new Data();

            //Aimed data
            nextData.setAimedPower(data.getAimedPower());
            nextData.setAimedRotation(data.getAimedRotation());

            //Next rotation
            nextData.setActualRotation(calculateNext(data.getActualRotation(), data.getAimedRotation(), absRotationIncrementMax));

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
         * @param data
         * @param aimedData
         * @param increment
         * @return
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

        private enum Position { OVER, UNDER, ON, UNKNOWN }

        public static Position getPositionRelativeToGround(PreciseCoordinate c, List<Zone> zones) {
            Zone zone = getZone(c.getX(), zones);
            if (zone != null) {
                if (zone.getStart().getY() == zone.getEnd().getY()) {
                    //Flat zone
                    if (c.getY() > zone.getStart().getY()) {
                        return Position.OVER;
                    } else if (c.getY() < zone.getStart().getY()) {
                        return Position.UNDER;
                    } else {
                        return Position.ON;
                    }
                } else {
                    double altitude = getAltitude(c.getX(), );
                    if (c.getY() > altitude) {
                        return Position.OVER;
                    } else if (c.getY() < altitude) {
                        return Position.UNDER;
                    } else {
                        return Position.ON;
                    }
                }
            }
            return Position.UNKNOWN;
        }

        private static Zone getZone(double x, List<Zone> zones) {
            for (Zone zone : zones) {
                if (x >= zone.getStart().getX() && x <= zone.getEnd().getX()) {
                    return zone;
                }
            }
            return null;
        }

        private static double getAltitude(double x, Zone zone) {
            double coefficient = (x - zone.getStart().getX()) / (double)(zone.getEnd().getX() - zone.getStart().getX());
            int altitudeDifference = zone.getStart().getY() - zone.getEnd().getY();
            return (double)zone.getStart().getY() -  coefficient * (double) altitudeDifference;
        }

        public static int calculateTimeToRotateToLand(int angle) {
            return (int) Math.ceil(Math.abs(angle) / absRotationIncrementMax);
        }

        /**
         * Dumb method
         * @param coordinate
         * @param data
         */
        public static void calculatePowerAndRotation(Coordinate coordinate, Data data) {
            if (Math.abs(data.getHSpeed()) > maxHSpeed) {

                int incrementAngle = (maxAbsRotation - Math.abs(data.getActualRotation())) / 3;;

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
                } else {
                    //No direction
                    if (data.getCurrentPosition().getX() > coordinate.getX()) {
                        data.setAimedRotation(data.getActualRotation() + incrementAngle);
                    } else if (data.getCurrentPosition().getX() < coordinate.getX()) {
                        data.setAimedRotation(data.getActualRotation() - incrementAngle);
                    }
                }
            }

            if (Math.abs(data.getVSpeed()) > maxVSpeed) {
                if (data.getActualPower() < maxPower) {
                    data.setAimedPower(data.getActualPower() + 1);
                }
            }
        }

        public static boolean exploreNextSolution(Data data, Node<Data> parentNode, PreciseCoordinate landingCoordinate, List<Zone> zones) {
            System.err.println("Try solution : " + data.toString());

            CalculatedData calculatedData = new CalculatedData();
            estimateLandingData(data, calculatedData, zones);
            System.err.println("Calculated data : " + calculatedData.toString());

            if (data) {
                return true;
            }

            //Exit causes
            if (calculatedData.getEstimatedTimeToLand() == 0) {
                //On the ground (or under) without found a solution
                return false;
            }
            if (data.getCurrentPosition().getX() < zones.get(0).getStart().getX() || data.getCurrentPosition().getX() > zones.get(zones.size()-1).getEnd().getX()) {
                //Going out of zones
                return false;
            }
            if (data.getCurrentPosition().getY() > 6999) {
                //Too high
                return false;
            }
            double divergence = landingCoordinate.getX() - calculatedData.getEstimatedCoordinateToLand().getX();

            boolean solutionFound;
            int minLimitPower = data.getActualPower() > minPower ? data.getActualPower() - 1 : data.getActualPower();
            int maxLimitPower = data.getActualPower() < maxPower ? data.getActualPower() + 1 : data.getActualPower();

            System.err.println("Try power from " + minLimitPower + " to " + maxLimitPower);
            for (int power = minLimitPower; power <= maxLimitPower; power = power + absPowerIncrement) {
                Data tmpData = new Data().clone(data);
                tmpData.setAimedPower(power);
                tmpData.setDivergence(divergence);
                tmpData = calculateNextData(tmpData);
                Node child = new Node(tmpData, parentNode);
                solutionFound = exploreNextSolution(tmpData, child, landingCoordinate, zones);
                if (solutionFound){
                    return true;
                } else {
                    parentNode.removeChild();
                }
            }


            int minLimitRotation;
            if (divergence < 0 && data.getHSpeed() > 0) {
                //Drift to the right -> prohibit negative rotation
                minLimitRotation = data.getActualRotation();
            } else {
                if (data.getActualRotation() > absRotationIncrementMax - maxAbsRotation) {
                    minLimitRotation = data.getActualRotation() - absRotationIncrementMax;
                } else {
                    minLimitRotation = -maxAbsRotation;
                }
            }

            int maxLimitRotation;
            if (divergence > 0 && data.getHSpeed() < 0) {
                //Drift to the left -> prohibit positive rotation
                maxLimitRotation = data.getActualRotation();
            } else {
                if (data.getActualRotation() < maxAbsRotation - absRotationIncrementMax) {
                    maxLimitRotation = data.getActualRotation() + absRotationIncrementMax;
                } else {
                    maxLimitRotation = maxAbsRotation;
                }
            }

            System.err.println("Try rotation from " + minLimitRotation + " to " + maxLimitRotation);
            for (int rotation = minLimitRotation; rotation <= maxLimitRotation; rotation = rotation + absRotationIncrementUnit) {
                Data tmpData = new Data().clone(data);
                tmpData.setAimedRotation(rotation);
                tmpData.setDivergence(divergence);
                tmpData = calculateNextData(tmpData);
                Node child = new Node(tmpData, parentNode);
                solutionFound = exploreNextSolution(tmpData, child, landingCoordinate, zones);
                if (solutionFound){
                    return true;
                } else {
                    parentNode.removeChild();
                }
            }
            return false;
        }

        public static boolean exploreNextSolution2(Data data, Node<Data> parentNode, PreciseCoordinate landingCoordinate, List<Zone> zones) {
            System.err.println("Try solution : " + data.toString());

            CalculatedData calculatedData = new CalculatedData();
            estimateLandingData(data, calculatedData, zones);

            if (calculatedData.getEstimatedTimeToLand() == 0) {
                //On the ground (or under) without found a solution
                return false;
            }

            System.err.println("Calculated data : " + calculatedData.toString());

            double divergence = landingCoordinate.getX() - calculatedData.getEstimatedCoordinateToLand().getX();
            boolean solutionFound = false;

            if (Math.abs(divergence) > 1) {
                int minLimitPower = data.getActualPower() > minPower ? data.getActualPower() - 1 : data.getActualPower();
                int maxLimitPower = data.getActualPower() < maxPower ? data.getActualPower() + 1 : data.getActualPower();
                int minLimitRotation = data.getActualRotation() > absRotationIncrementMax - maxAbsRotation ? data.getActualRotation() - absRotationIncrementMax : - maxAbsRotation;
                int maxLimitRotation = data.getActualRotation() < maxAbsRotation - absRotationIncrementMax ? data.getActualRotation() + absRotationIncrementMax : maxAbsRotation;

                for (int power = minLimitPower; power < maxLimitPower && !solutionFound; power = power + absPowerIncrement) {
                    for (int rotation = minLimitRotation; rotation < maxLimitRotation && !solutionFound; rotation = rotation + absRotationIncrementUnit) {
                        Data tmpData = new Data().clone(data);
                        Node child = new Node(tmpData, parentNode);
                        tmpData.setAimedPower(power);
                        tmpData.setAimedRotation(rotation);
                        tmpData = calculateNextData(tmpData);
                        solutionFound = exploreNextSolution(tmpData, child, landingCoordinate, zones);
                    }
                }
                if (solutionFound){
                    return true;
                }
                return false;
            } else {
                if (isPossibleLanding(calculatedData)) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        private static boolean isPossibleLanding(CalculatedData calculatedData) {
            return Math.abs(calculatedData.getLandingHSpeed()) < maxHSpeed && Math.abs(calculatedData.getLandingVSpeed()) < maxVSpeed;
        }

        public static void processPowerAndRotation(Data data, Node<Data> root) {
            if (root != null) {
                data.setAimedRotation(root.getData().getAimedRotation());
                data.setAimedPower(root.getData().getAimedPower());
                root = root.getChild();
            }
        }
    }

    public static class Coordinate {
        private int x;
        private int y;

        public Coordinate() {
        }

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Coordinate clone(Coordinate c) {
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

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[").append(x).append(",").append(y).append("]");
            return stringBuilder.toString();
        }
    }

    public static class Zone {
        private Coordinate start;
        private Coordinate end;

        public Zone() {
            start = new Coordinate(-1, -1);
            end = new Coordinate(-1, -1);
        }

        public Zone(Coordinate x1, Coordinate x2) {
            this.start = x1;
            this.end = x2;
        }

        public Coordinate getStart() {
            return start;
        }

        public Coordinate getEnd() {
            return end;
        }

        public void setStart(Coordinate x) {
            this.start = x;
        }

        public void setEnd(Coordinate x) {
            this.end = x;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{").append(start.toString()).append("-").append(end.toString()).append("}");
            return stringBuilder.toString();
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
        private double divergence;

        public Data() {
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder("[");
            stringBuilder.append("Actual rotation : " + actualRotation + " ; ");
            stringBuilder.append("Aimed rotation : " + aimedRotation + " ; ");
            stringBuilder.append("Actual power : " + actualPower + " ; ");
            stringBuilder.append("Aimed power : " + aimedPower + " ; ");
            stringBuilder.append("Current position : " + currentPosition.toString() + " ; ");
            stringBuilder.append("V speed : " + Math.round(vSpeed) + " ; ");
            stringBuilder.append("H speed : " + Math.round(hSpeed));
            return stringBuilder.append("]").toString();
        }

        public Data clone(Data d) {
            this.actualRotation = d.getActualRotation();
            this.aimedRotation = d.getAimedRotation();
            this.actualPower = d.getActualPower();
            this.aimedPower = d.getAimedPower();
            this.currentPosition = (new PreciseCoordinate()).clone(d.getCurrentPosition());
            this.hSpeed = d.getHSpeed();
            this.vSpeed = d.getVSpeed();
            this.divergence = d.getDivergence();
            return this;
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

        public double getDivergence()
        {
            return divergence;
        }

        public void setDivergence(double divergence)
        {
            this.divergence = divergence;
        }
    }

    public static class CalculatedData {
        private int estimatedTimeToLand;
        private PreciseCoordinate estimatedCoordinateToLand;
        private double landingVSpeed;
        private double landingHSpeed;

        public CalculatedData() {
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder("[");
            stringBuilder.append("Estimated time to land : " + estimatedTimeToLand + " ; ");
            stringBuilder.append("Estimated coordinate to land : " + estimatedCoordinateToLand + " ; ");
            stringBuilder.append("Estimated vSpeed landing : " + landingVSpeed + " ; ");
            stringBuilder.append("Estimated hSpeed landing : " + landingHSpeed);
            return stringBuilder.append("]").toString();
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

        public double getLandingVSpeed() {
            return landingVSpeed;
        }

        public void setLandingVSpeed(double landingVSpeed) {
            this.landingVSpeed = landingVSpeed;
        }

        public double getLandingHSpeed() {
            return landingHSpeed;
        }

        public void setLandingHSpeed(double landingHSpeed) {
            this.landingHSpeed = landingHSpeed;
        }
    }

    public static class Vector {
        private double x;
        private double y;

        public Vector(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[").append(x).append(",").append(y).append("]");
            return stringBuilder.toString();
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }

    public static class PreciseCoordinate {
        private double x;
        private double y;

        public PreciseCoordinate() {
        }

        public PreciseCoordinate(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public PreciseCoordinate clone(PreciseCoordinate coordinate) {
            PreciseCoordinate preciseCoordinate = new PreciseCoordinate(coordinate.getX(), coordinate.getY());
            return preciseCoordinate;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[").append(df.format(x)).append(";").append(df.format(y)).append("]");
            return stringBuilder.toString();
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }

    public static class Node<Data> {
        private Node<Data> child = null;
        private Node<Data> parent = null;
        private Data data = null;

        public Node(Data data) {
            this.data = data;
        }

        public Node(Data data, Node<Data> parent) {
            this.data = data;
            this.parent = parent;
        }

        public Node<Data> getChild() {
            return child;
        }

        public void setParent(Node<Data> parent) {
            parent.setChild(this);
            this.parent = parent;
        }

        public void setChild(Data data) {
            Node<Data> child = new Node<Data>(data);
            child.setParent(this);
            this.child = child;
        }

        public void setChild(Node<Data> child) {
            child.setParent(this);
            this.child = child;
        }

        public Data getData() {
            return this.data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        public boolean isRoot() {
            return (this.parent == null);
        }

        public boolean isLeaf() {
            if(this.child == null)
                return true;
            else
                return false;
        }

        public void removeParent() {
            this.parent = null;
        }

        public void removeChild() {
            this.child = null;
        }
    }
}