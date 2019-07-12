package emufog.util;

public class ConversionsUtils {

    public static String intervalToString(long start, long end) {
        double d = (double) (end - start) / 1000000;

        return d + "ms";
    }
}
