package to.etc.domui.converter;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;

public class NumericUtil {
	private NumericUtil() {}

	public static <T> IConverter<T> createNumberConverter(Class<T> type, NumericPresentation np) {
		if(DomUtil.isIntegerOrWrapper(type)) {



		} else
			throw new IllegalArgumentException("Cannot convert numeric string to type=" + type);
		return null;
	}
}
