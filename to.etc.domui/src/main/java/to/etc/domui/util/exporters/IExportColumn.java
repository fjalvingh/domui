package to.etc.domui.util.exporters;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public interface IExportColumn {
	@Nullable String getLabel();

	@Nonnull Class<?> getActualType();

	@Nullable
	IExportCellRenderer<?, ?, ?> getRenderer();

	@Nullable
	Object convertValue(@Nullable Object value);
}
