package to.etc.domui.util.importers;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public interface IImportColumn {
	@Nullable String getName();

	@Nullable String getStringValue();

	@Nullable Date asDate();

	@Nullable Date asDate(@NonNull String dateFormat);

	@Nullable Long asLong();

	@Nullable Integer asInteger();

	@Nullable BigDecimal getDecimal();
}
