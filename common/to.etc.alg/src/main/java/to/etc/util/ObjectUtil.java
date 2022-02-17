package to.etc.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public final class ObjectUtil {

	/**
	 * Check if first value is equal to any of other values.
	 * Use it for primitive types.
	 */
	static public <T> boolean isIn(T value, T... values) {
		for(T item : values) {
			if(item.equals(value)) {
				return true;
			}
		}
		return false;
	}
}
