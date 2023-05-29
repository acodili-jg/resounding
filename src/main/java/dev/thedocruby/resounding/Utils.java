package dev.thedocruby.resounding;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.openal.EXTEfx;

import dev.thedocruby.resounding.effects.Effect;

/**
 * Resounding's utility class.
 */
public class Utils {
	/**
	 * A specialized 3-tuple consisting of the types {@link String}, {@code int}, and {@code
	 * float}; it's usually used for sound effects.
	 *
	 * @param f the first element; usually a name for {@linkplain #s an EAX reverb parameter ID}
	 * @param s the second element; usually an EAX reverb parameter ID (See: {@link EXTEfx})
	 * @param t the third element; usually the argument of an AL effect paramter
	 */
	public record SIF(String f, int s, float t) {
		/**
		 * Constructs a new {@link String}-{@code int}-{@code float} 3-tuple.
		 *
		 * @param f the first element
		 * @param s the second element
		 * @param t the third element
		 */
		public SIF {}
	}

	// java's type system sucks... overloading, ugh - even python could do better...
	// an overloaded array length extender function boolean[], boolean[][], int[], int[][] {

	/**
	 * Returns an extended copy of an {@code boolean[]}, with {@code false} as a filler, twice its
	 * original length reduced by the constant {@code min}, but never shorter than or equal to the
	 * original.
	 *
	 * @param old the original array
	 * @param min the amount to reduce from twice the original length
	 * @return an extended copy of the original array
	 * @implNote This method allows: {@code null} arrays to be extended as they are considered as
	 *           empty; and, negative values for {@code min}, allowing the original array to extend
	 *           past twice it's length.
	 */
	public static boolean[] extendArray(final boolean[] old, final int min) {
		final var extensionLength = Math.max(1, old.length - min);
		final var extension = new boolean[extensionLength];

		return ArrayUtils.addAll(old, extension);
	}

	/**
	 * Returns an extended copy of an {@code boolean[][]}, with {@code null} as a filler, twice its
	 * original length reduced by the constant {@code min}, but never shorter than or equal to the
	 * original.
	 *
	 * @param old the original array
	 * @param min the amount to reduce from twice the original length
	 * @return an extended copy of the original array
	 * @implNote This method allows: {@code null} arrays to be extended as they are considered as
	 *           empty; and, negative values for {@code min}, allowing the original array to extend
	 *           past twice it's length.
	 */
	public static boolean[][] extendArray(final boolean[][] old, final int min) {
		final var extensionLength = Math.max(1, old.length - min);
		final var extension = new boolean[extensionLength][];

		return ArrayUtils.addAll(old, extension);
	}

	/**
	 * Returns an extended copy of an {@code int[]}, with {@code 0} as a filler, twice its original
	 * length reduced by the constant {@code min}, but never shorter than or equal to the original.
	 *
	 * @param old the original array
	 * @param min the amount to reduce from twice the original length
	 * @return an extended copy of the original array
	 * @implNote This method allows: {@code null} arrays to be extended as they are considered as
	 *           empty; and, negative values for {@code min}, allowing the original array to extend
	 *           past twice it's length.
	 */
	public static int[] extendArray(final int[] old, final int min) {
		final var extensionLength = Math.max(1, old.length - min);
		final var extension = new int[extensionLength];

		return ArrayUtils.addAll(old, extension);
	}

	/**
	 * Returns an extended copy of an {@code int[][]}, with {@code null} as a filler, twice its
	 * original length reduced by the constant {@code min}, but never shorter than or equal to the
	 * original.
	 *
	 * @param old the original array
	 * @param min the amount to reduce from twice the original length
	 * @return an extended copy of the original array
	 * @implNote This method allows: {@code null} arrays to be extended as they are considered as
	 *           empty; and, negative values for {@code min}, allowing the original array to extend
	 *           past twice it's length.
	 */
	public static int[][] extendArray(final int[][] old, final int min) {
		final var extensionLength = Math.max(1, old.length - min);
		final var extension = new int[extensionLength][];

		return ArrayUtils.addAll(old, extension);
	}
}
