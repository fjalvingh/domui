package to.etc.domui.util.importers;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Date;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public interface IImportColumn {
	@Nullable String getName();

	@Nullable String getStringValue();

	@Nullable Date asDate();

	@Nullable Long asLong();

	@Nullable Integer asInteger();
}
