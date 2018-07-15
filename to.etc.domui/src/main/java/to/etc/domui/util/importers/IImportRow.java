package to.etc.domui.util.importers;

import org.eclipse.jdt.annotation.NonNull;

import java.io.IOException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public interface IImportRow {
	int getColumnCount();

	@NonNull
	IImportColumn get(int index) throws IOException;

	@NonNull
	IImportColumn get(@NonNull String name) throws IOException;
}
