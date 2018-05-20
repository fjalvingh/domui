package to.etc.domui.util.exporters;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public interface IExportCellRenderer<X extends IExportWriter<?>, C, V> {
	void renderCell(@NonNull X exporter, @NonNull C cell, int cellIndex, @Nullable V value) throws Exception;
}
