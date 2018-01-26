package to.etc.domui.util.exporters;

import to.etc.domui.util.IValueTransformer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public interface IExportColumn<T> extends IValueTransformer<T> {
	@Nullable String getLabel();

	@Nonnull Class<?> getActualType();

	@Nullable
	default IExportCellRenderer<?, ?, ?> getRenderer() {
		return null;
	}

	@Nullable
	default Object convertValue(@Nullable Object value) {
		return value;
	}
}
