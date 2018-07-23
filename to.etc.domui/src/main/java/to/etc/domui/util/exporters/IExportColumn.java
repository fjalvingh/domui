package to.etc.domui.util.exporters;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.util.IValueTransformer;

/**
 *
 * @param <T>	The actual type of the column value as returned by the property getter.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public interface IExportColumn<T> extends IValueTransformer<T> {
	@Nullable String getLabel();

	@NonNull Class<?> getActualType();

	@Nullable
	default IExportCellRenderer<?, ?, ?> getRenderer() {
		return null;
	}

	@Nullable
	default Object convertValue(@Nullable Object value) {
		return value;
	}
}
