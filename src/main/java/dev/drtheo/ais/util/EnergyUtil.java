package dev.drtheo.ais.util;

public class EnergyUtil {

    private static final int RATE = 1000;

    public static long toBotarium(double artron) {
        return (long) (artron * RATE);
    }

    public static double toArtron(long energy) {
        return (double) energy / RATE;
    }
}
