package org.ymkm.pwchart;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.util.SparseIntArray;

import java.util.Map;

public final class PWChartUtils {

    public static final int CHART_TYPE_NORMAL = 0x0;
    public static final int CHART_TYPE_INCLUDE_NUMBERS = 0x1;
    public static final int CHART_TYPE_INCLUDE_SYMBOLS = 0x2;


    private static final long R = 0x100000000L;
    private static final char[] CHAR_MAP = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final SparseIntArray CHART_TYPE_MAPPING = new SparseIntArray() {

        {
            put(CHART_TYPE_NORMAL, 1);
            put(CHART_TYPE_INCLUDE_NUMBERS, 2);
            put(CHART_TYPE_INCLUDE_SYMBOLS, 3);
            put(CHART_TYPE_INCLUDE_NUMBERS | CHART_TYPE_INCLUDE_SYMBOLS, 4);
        }
    };


    public static MersenneTwister newMT(@NonNull final String seed) {
        final long hash = MD5.seedHash(seed);
        final MersenneTwister mt = new MersenneTwister(hash);
        return mt;
    }

    public static long chartNumber(@NonNull final String seed, final int chartType) {
        final long hash = MD5.seedHash(seed);
        return hash * CHART_TYPE_MAPPING.get(chartType, CHART_TYPE_NORMAL);
    }

    public static String genPasswordWithSeed(@NonNull final String seed, @NonNull final String pw, final boolean useNumbers, final boolean usePunct) {
        final StringBuilder genPw = new StringBuilder();
        final Map<Character, String> table = getTable(seed, useNumbers, usePunct);
        for (final char c : pw.toCharArray()) {
            genPw.append(table.get(c));
        }
        return genPw.toString();
    }

    public static ArrayMap<Character, String> getTable(@NonNull final String seed, final boolean useNumbers, final boolean usePunct) {
        final MersenneTwister mt = newMT(seed);
        final ArrayMap<Character, String> table = new ArrayMap<>();
        for (int i = 0 ; i < CHAR_MAP.length ; ++i) {
            table.put(CHAR_MAP[i], getGarbage(mt, useNumbers, usePunct));
        }
        return table;
    }


    public static int randomInRange(@NonNull final MersenneTwister mt, final int lower, final int upper) {
        final int range = upper - lower;
        long rand = mt.next(32);
        if (((int) rand) < 0) {
            rand = ((int) rand) + R;
        }
        final double f = (double) rand / 0xFFFFFFFFL;
        final int x = (int) (lower + Math.round(range * f));
        return x;
    }


    private static char getLetter(@NonNull final MersenneTwister mt) {
        if (randomInRange(mt, 0, 1) == 1) {
            // don't use l or o as they look like the numbers 1 and 0
            return "abcdefghijkmnpqrstuvwxyz".charAt(randomInRange(mt, 0, 23));
        }
        else
        // don't use O and Z as they look like 0 and 2
        {
            return "ABCDEFGHIJKLMNPQRSTUVWXY".charAt(randomInRange(mt, 0, 23));
        }
    }


    private static char getNumber(@NonNull final MersenneTwister mt) {
        // don't use 0, 1, 2 and 5 as they look like letters
        return "346789".charAt(randomInRange(mt, 0, 5));
    }

    private static char getPunct(@NonNull final MersenneTwister mt) {
        return "~!@#$%^&*_+?".charAt(randomInRange(mt, 0, 11));
    }

    private static char getCharacter(@NonNull final MersenneTwister mt, final boolean useNumbers, final boolean usePunct) {
        while (true) {
            switch (randomInRange(mt, 0, 3)) {
                case 0:
                case 1:
                    return getLetter(mt);
                case 2:
                    if (useNumbers) {
                        return getNumber(mt);
                    }
                    break;
                case 3:
                    if (usePunct) {
                        return getPunct(mt);
                    }
                    break;
            }
        }
    }

    private static String getGarbage(@NonNull final MersenneTwister mt, final boolean useNumbers, final boolean usePunct) {
        StringBuilder garbage = new StringBuilder(4);
        final int len = randomInRange(mt, 1, 3);
        for (int i = 0; i < len; ++i) {
            final char character = getCharacter(mt, useNumbers, usePunct);
            garbage.append(character);
        }
        return garbage.toString();
    }


    private PWChartUtils() {}
}
