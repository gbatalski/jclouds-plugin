/**
 *
 */
package edu.kit.aifb.gb.utils;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Iterables.*;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

/**
 * @author gena
 *
 */
public class FilterIntegers {
    /**
     * Filters out non-integer values from iterable of @param strings
     *
     * This method skips the not convertable values and nulls
     *
     * @return Iterable of Integer representations of @param strings
     *
     */
    public static Iterable<Integer> filterIntegers(Iterable<String> strings) {
	return filter(transform(strings, new Function<String, Integer>() {
	    @Override
	    public Integer apply(String input) {
		return nullToEmpty(input).trim().matches("\\d+") ? Integer.parseInt(input.trim()) : null;
	    }

	}), Predicates.notNull());
    }

    /**
     * Filters out non-integer values from string @param stringOfIntegers
     *
     * the applied split pattern is: [\s|,|;]
     *
     * This method skips the not convertable values and nulls
     *
     * @return Iterable of Integer representations of @param strings
     *
     */
    public static Iterable<Integer> filterIntegers(String stringOfIntegers) {
	return stringOfIntegers == null ? ImmutableSet.<Integer> of() : filterIntegers(Splitter.onPattern("\\s|,|;").split(
		stringOfIntegers));
    }
}
