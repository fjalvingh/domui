package to.etc.domui.util.exporters;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public interface IExportCellRenderer<X extends IExportWriter<?>, C, V> {
	void renderCell(@Nonnull X exporter, @Nonnull C cell, int cellIndex, @Nullable V value) throws Exception;
}
