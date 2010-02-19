package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.trouble.*;
import to.etc.util.*;

public class MsDurationConverter implements IConverter<Long> {
	@Override
	public String convertObjectToString(Locale loc, Long in) throws UIException {
		if(in == null)
			return "";
		if(in.longValue() < 0)
			return "";
		return StringTool.strDurationMillis(in.longValue());
	}

	@Override
	public Long convertStringToObject(Locale loc, String in) throws UIException {
		throw new IllegalStateException("Not implemented yet");
	}
}
