package to.etc.webapp.query;

/**
 * This is the result of the or() call on a parent RestrictionBase; it adds all of the items as a set of
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2009
 */
public class QOr<T> {
	private QRestrictionBase<T> m_dad;

	protected QOr(QRestrictionBase<T> parent) {
		m_dad = parent;
	}

	/**
	 * Create the next leaf: the next "or" or the next "and" in the parent; use the restrictions passed to add
	 * individual
	 * @return
	 */
	public QRestrictionBase<T> nextOR() {
		return null;
	}


}
