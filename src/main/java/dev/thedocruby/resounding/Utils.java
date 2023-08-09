package dev.thedocruby.resounding;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
* Utils
*/
public class Utils {
    public static final Logger LOGGER = LogManager.getLogger("Resounding");

// java's type system sucks... overloading, ugh - even python could do better...
// an overloaded array length extender function boolean[], boolean[][], int[], int[][] {

public static int[]       extendArray (final int[]       old, final int min) {
	return ArrayUtils.addAll(old, new int    [Math.max(1,old.length - min)]  );
}

public static int[][]     extendArray (final int[][]     old, final int min) {
	return ArrayUtils.addAll(old, new int    [Math.max(1,old.length - min)][]);
}

public static boolean[]   extendArray (final boolean[]   old, final int min) {
	return ArrayUtils.addAll(old, new boolean[Math.max(1,old.length - min)]  );
}

public static boolean[][] extendArray (final boolean[][] old, final int min) {
	return ArrayUtils.addAll(old, new boolean[Math.max(1,old.length - min)][]);
}
    public static <IN,OUT> OUT memoize(HashMap<String, IN> in, HashMap<String, OUT> out, String key, BiFunction<Function<String, OUT>, IN, OUT> calculate) {
        return memoize(in, out, key, calculate, true);
    }

    // specialized memoization for tag/material cache functionality
    public static <IN,OUT> OUT memoize(HashMap<String, IN> in, HashMap<String, OUT> out, String key, BiFunction<Function<String, OUT>, IN, OUT> calculate, boolean remove) {
        // return cached values
        if (out.containsKey(key))
            return out.get(key);
        // mark as in-progress
        // getter == null; should be scanned for in calculate to prevent cyclic references
        out.put(key, null);
        OUT value = calculate.apply(
                x -> memoize(in, out, x, calculate, remove),
                remove ? in.remove(key) : in.get(key));
        out.put(key, value);
        if (value == null)
            LOGGER.error("{} is invalid or cyclical", key);
        return value;
    }

    // ba-d-ad jokes will never get old!
    // update + weight
    public static void updWeight(Double[] list, int index, @Nullable Double value, double coefficient) {
        if (list != null)
            list[index] = value == null ? 0 : value * coefficient;
    }

    // average values using weights
    public static Double unWeight(Double weight, Double[] values, Double fallback) {
        // density == 0 is for single-value modifications (shapes, lone values)
        return smartSum(values, fallback) / (weight == 0 ? values.length : weight);
    }

    // sum with specialized fallback
    public static Double smartSum(Double[] values, Double fallback) {
        if (values == null)
            return fallback;
        double output = 0;
        for (Double value : values)
            output += value == null ? 0 : value;
        return output;
    }

    // update a value in a particular type of hashmap (see signature)
    public static void update(HashMap<String, LinkedList<String>> map, String key, String value) {
        final LinkedList<String> list = map.getOrDefault(key, new LinkedList<>());
        list.add(value);
        map.put(key, list);
    }

    /* utility function */
    public static <T> double logBase(T x, T b) { return Math.log((Double) x) / Math.log((Double) b); }

// }

	// specialized tuple type for effects using float
	public record SIF
		( String f // first
		, int    s // second
		, float  t  // third
	) {}

}
