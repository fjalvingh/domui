package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.trouble.*;
import to.etc.util.*;

public class IntSizeConverter implements IConverter {
	public String convertObjectToString(Locale loc, Object in) throws UIException {
		Integer val = (Integer) in;
		return StringTool.strSize(val.longValue());
	}

	public Object convertStringToObject(Locale loc, String in) throws UIException {
		throw new IllegalStateException("Not implemented yet");
	}
}
