package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * Converter to convert a computer "size" to a size in bytes, KB, MB, GB etc.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 30, 2009
 */
public class LongSizeConverter implements IConverter<Long> {
	public String convertObjectToString(Locale loc, Long in) throws UIException {
		return StringTool.strSize(in.longValue());
	}

	public Long convertStringToObject(Locale loc, String val) throws UIException {
		val = val.trim();
		if(val.length() == 0)
			throw new ValidationException(Msgs.V_INVALID);

		int lindex = val.length();
		while(lindex > 0 && !Character.isDigit(val.charAt(lindex - 1)))
			lindex--;
		if(lindex <= 0)
			throw new ValidationException(Msgs.V_INVALID);

		String f = val.substring(lindex).toLowerCase();
		val = val.substring(0, lindex);
		double size = Double.parseDouble(val);
		if("k".equals(f))
			size *= 1024;
		else if("m".equals(f))
			size *= 1024 * 1024;
		else if("g".equals(f))
			size *= 1024l * 1024l * 1024l;
		else if("t".equals(f))
			size *= 1024l * 1024l * 1024l * 1024l;
		else if(f.length() == 0)
			;
		else
			throw new ValidationException(Msgs.V_INVALID);

		return Long.valueOf((long) size);
	}
}
