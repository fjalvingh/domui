package to.etc.domui.util.importers;

import javax.annotation.Nullable;
import java.util.Date;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-12-17.
 */
abstract public class AbstractImportColumn implements IImportColumn {
	@Nullable @Override abstract public String getStringValue();

	@Nullable @Override abstract public Date asDate();

	@Nullable @Override public Long asLong() {
		String stringValue = getStringValue();
		if(null == stringValue)
			return null;
		return Long.valueOf(stringValue);
	}

	@Nullable @Override public Integer asInteger() {
		String stringValue = getStringValue();
		if(null == stringValue)
			return null;
		return Integer.valueOf(stringValue);
	}

}
