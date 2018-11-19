package graph;

import java.time.Duration;
import java.time.LocalTime;

public class TimeService {
    private static LocalTime start;

    public static void start() {
        start = LocalTime.now();
    }

    public static long getElapsedTime() {
        if (start == null) {
            return 0;
        }
        return Duration.between(start, LocalTime.now()).toMillis();
    }
}
