package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

public class BooleanConverter implements IConverter<Boolean> {
	static private final BooleanConverter m_instance = new BooleanConverter();

	@Override
	public Boolean convertStringToObject(Locale loc, String in) throws UIException {
		throw new IllegalStateException("Unreasonable text conversion from string to boolean.");
	}

	@Override
	public String convertObjectToString(Locale loc, Boolean in) throws UIException {
		if(in == null)
			return null;
		return Msgs.BUNDLE.getString(loc, in.booleanValue() ? Msgs.UI_BOOL_TRUE : Msgs.UI_BOOL_FALSE);
	}

	static public final BooleanConverter getInstance() {
		return m_instance;
	}
}
