package to.etc.webapp.query;

import java.lang.reflect.*;

import javax.annotation.*;

/**
 * Class for creating exists queries based upon the list in the Entity class.
 *
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 * Created on Feb 3, 2013
 */
public class QList<R extends QField<R, ? >> {


	private @Nonnull R m_root;

	private @Nonnull QField< ? , ? > m_parent;

	@Nonnull
	String m_listName;

	public QList(@Nonnull R root, @Nonnull QField< ? , ? > parent, String listName) throws Exception {
		m_root = root;
		m_root.m_isSub = true;
		m_parent = parent;
		m_listName = listName;
	}

	public @Nonnull
	<T>
	R exists() throws Exception {

		Class<T> rootClass = (Class<T>) getRootClass();
		final QExistsSubquery<T> sq = new QExistsSubquery<T>(m_parent.criteria().getBaseClass(), rootClass, m_listName);
		QRestrictor<T> builder = new QRestrictor<T>(rootClass, QOperation.AND) {
			@Override
			public QOperatorNode getRestrictions() {
				return sq.getRestrictions();
			}

			@Override
			public void setRestrictions(QOperatorNode n) {
				sq.setRestrictions(n);
			}
		};

		m_parent.addNode(sq);
		m_root.m_criteria = cast(builder);
		return m_root;
	}

	/**
	 * dirty trick ;(
	 * @param t
	 * @return
	 */
	<T> T cast(Object t) {
		return (T) t;
	}

	@Nonnull Class< ? > getRootClass() {
		return  (Class<?>) ((ParameterizedType) m_root.getClass().getSuperclass().getGenericSuperclass()).getActualTypeArguments()[1];
	}
}
