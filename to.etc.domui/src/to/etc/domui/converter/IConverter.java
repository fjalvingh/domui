package to.etc.domui.converter;

import java.util.Locale;

import to.etc.domui.trouble.UIException;

/**
 * Basic marker interface for the allowed converters. All converters here convert Strings
 * to whatever types and v.v.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 11, 2008
 */
public interface IConverter {
	public Object		convertStringToObject(Locale loc, String in) throws UIException;
	public String		convertObjectToString(Locale loc, Object in) throws UIException;
}
