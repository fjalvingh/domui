package to.etc.domui.util.importers;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public interface IImportRow {
	int getColumnCount();

	IImportColumn get(int index);
}
