package dev.xdark.ssvm.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;

/**
 * Slice of the array.
 *
 * @param <V>
 *      Component type.
 *
 * @author xDark
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArraySlice<V> {

	protected V[] array;
	private final int fromIndex;
	private final int toIndex;

	/**
	 * Sets element.
	 *
	 * @param index
	 *      Element index.
	 * @param value
	 *      Element value.
	 */
	public void set(int index, V value) {
		array[map(index)] = value;
	}

	/**
	 * @param index
	 *      Element index.
	 *
	 * @return element by it's index.
	 */
	public V get(int index) {
		return array[map(index)];
	}

	/**
	 * @return size of array slice.
	 */
	public int length() {
		return toIndex - fromIndex;
	}

	/**
	 * @return backing array.
	 */
	public V[] unwrap() {
		return Arrays.copyOfRange(array, fromIndex, toIndex);
	}

	/**
	 * @param fromIndex
	 *      Starting index.
	 * @param toIndex
	 *      Ending index.
	 *
	 * @return slice of this slice.
	 */
	public ArraySlice<V> slice(int fromIndex, int toIndex) {
		return new ArraySlice<>(array, map(fromIndex), map(toIndex));
	}

	protected int map(int index) {
		return index + fromIndex;
	}
}