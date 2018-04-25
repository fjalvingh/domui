package to.etc.domui.util.importers;

import javax.annotation.DefaultNonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-4-18.
 */
@DefaultNonNull
public interface IDatasetInfo {
	/**
	 * Return the name of this set in the source.
	 */
	String getName();

	/**
	 * Return the set number in the source.
	 */
	int getIndex();
}
