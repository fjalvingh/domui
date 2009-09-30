package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.trouble.*;

public class DummyConverter implements IConverter<Object> {
	public String convertObjectToString(Locale loc, Object in) throws UIException {
		return in == null ? null : String.valueOf(in);
	}

	public Object convertStringToObject(Locale loc, String in) throws UIException {
		return in;
	}
}
