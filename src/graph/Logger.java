package graph;

public class Logger {
    private static  boolean activated = false;

    public static void log(String message) {
        if (activated) {
            System.out.println(message);
        }
    }

    public static void jumpLine() {
        if (activated) {
            System.out.println("");
        }
    }
}
