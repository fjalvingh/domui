package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

public class DoubleConverter implements IConverter<Double> {
	@Override
	public String convertObjectToString(Locale loc, Double in) throws UIException {
		if(in == null)
			return "";
		return String.format(loc, "%.3g", in);
	}

	@Override
	public Double convertStringToObject(Locale loc, String input) throws UIException {
		if(input == null)
			return null;
		input = input.trim();
		if(input.length() == 0)
			return null;
		try {
			return Double.valueOf(input);
		} catch(Exception x) {
			throw new ValidationException(Msgs.V_INVALID_DOUBLE);
		}
	}
}
