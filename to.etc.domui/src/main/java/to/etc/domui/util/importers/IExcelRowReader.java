package to.etc.domui.util.importers;

import org.eclipse.jdt.annotation.Nullable;

import java.text.DateFormat;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-01-22.
 */
public interface IExcelRowReader {
	@Nullable
	DateFormat getDefaultDateFormat();

	String convertDouble(double value);

	DateFormat getDateFormat(String dateFormat);

	@Nullable
	DateFormat getForceStringDateFormat();
}
