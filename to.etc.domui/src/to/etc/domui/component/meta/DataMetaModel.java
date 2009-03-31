package to.etc.domui.component.meta;

import to.etc.domui.component.meta.impl.*;

/**
 * The root for all metamodel lookups. When fields of known classes are
 * used in the system this can be used to lookup data pertaining to the
 * fields.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 16, 2008
 */
public interface DataMetaModel {
	/**
	 * Called when the metadata for a single class is required. This method must
	 * add <b>all</b> metadata known for the class to the metamodel passed, or
	 * edit the existing data to represent the metadata better. This gets called
	 * only once for each class that metadata is requested for.
	 *
	 * @param dmm
	 */
	public void			updateClassMeta(DefaultClassMetaModel dmm);
}
