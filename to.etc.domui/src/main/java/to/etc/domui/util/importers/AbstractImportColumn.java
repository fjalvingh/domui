package to.etc.domui.util.importers;

import org.eclipse.jdt.annotation.Nullable;

import java.math.BigDecimal;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-12-17.
 */
abstract public class AbstractImportColumn implements IImportColumn {
	@Nullable
	@Override
	public Long asLong() {
		String stringValue = getStringValue();
		if(null == stringValue)
			return null;
		return Long.valueOf(stringValue);
	}

	@Nullable
	@Override
	public Integer asInteger() {
		String stringValue = getStringValue();
		if(null == stringValue)
			return null;
		return Integer.valueOf(stringValue);
	}

	@Nullable
	@Override
	public BigDecimal getDecimal() {
		String stringValue = getStringValue();
		if(null == stringValue)
			return null;
		stringValue = stringValue.trim().replace(',', '.');
		if(stringValue.isEmpty())
			return null;

		return new BigDecimal(stringValue);
	}

	/**
	 * Uncrap the data inside an excel string row, which often contains all kinds of shitty spaces.
	 */
	@Nullable
	public static String trimAllWS(String v) {
		if(null == v)
			return null;
		v = v.replaceAll("(^\\h*)|(\\h*$)", "");    // Replace all spaces, even nbsp's and others, sigh.
		v = v.replaceAll("(\\h+)", " ");    // Replace all inner spaces with " "
		return v;
	}

}
