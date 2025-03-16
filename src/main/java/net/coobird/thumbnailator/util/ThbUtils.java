/*
 * Copyright 2025 Jeff Hain
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.coobird.thumbnailator.util;

public final class ThbUtils {
	
	private ThbUtils() {
	}
	
	/**
	 * @param ref A reference.
	 * @return The specified reference (if it is non-null).
	 * @throws NullPointerException if the specified reference is null.
	 */
	public static <T> T requireNonNull(T ref) {
		if (ref == null) {
			throw new NullPointerException();
		}
		return ref;
	}
	
	/**
	 * @param min Min value.
	 * @param value A value.
	 * @param name Value's name.
	 * @return The specified value.
	 * @throws IllegalArgumentException if the specified value is < min.
	 */
	public static int requireSupOrEq(int min, int value, String name) {
		if (!(value >= min)) {
			throw new IllegalArgumentException(
				name + " [" + value + "] must be >= " + min);
		}
		return value;
	}
	
	/**
	 * @param min A value.
	 * @param max A value.
	 * @param a A value.
	 * @return min if a <= min, else max if a >= max, else a.
	 */
	public static int toRange(int min, int max, int a) {
		if (a <= min) {
			return min;
		} else if (a >= max) {
			return max;
		} else {
			return a;
		}
	}
}
