package to.etc.domui.util.importers;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public interface IImportRow {
	int getColumnCount();

	@Nonnull
	IImportColumn get(int index);

	@Nonnull
	IImportColumn get(String name) throws IOException;
}
