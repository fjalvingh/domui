package to.etc.webapp.query;

import java.lang.reflect.*;

import javax.annotation.*;

/**
 * Class for creating exists queries based upon the list in the Entity class.
 *
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 * Created on Feb 3, 2013
 */
public class QList<P extends QField<P, ? >, R extends QField<R, ? >> {


	private @Nonnull R m_root;

	@Nonnull
	QField<P, ? > m_parent;

	@Nullable
	private QExistsSubquery< ? > m_subquery;

	@Nonnull
	String m_listName;

	public QList(@Nonnull R root, @Nonnull QField<P, ? > parent, String listName) throws Exception {
		m_root = root;
		m_root.m_isSub = true;
		m_parent = parent;
		m_listName = parent.toString().equals("") ? listName : parent.toString() + "." + listName;
	}

	public @Nonnull
	<T>
	R exists() throws Exception {

		Class<T> rootClass = (Class<T>) getRootClass();
		m_subquery = new QExistsSubquery<T>(m_parent.criteria(), rootClass, m_listName);
		m_parent.qbrace().add(this);
		return m_root;
	}


	@Nonnull Class< ? > getRootClass() {
		return  (Class<?>) ((ParameterizedType) m_root.getClass().getSuperclass().getGenericSuperclass()).getActualTypeArguments()[1];
	}

	@Nonnull
	QExistsSubquery< ? > getSubquery() {
		QExistsSubquery< ? > subquery = m_subquery;
		if(null == subquery)
			throw new IllegalStateException("Subquery is not defined");
		return subquery;
	}

	@Nonnull
	R getRoot() {
		return m_root;
	}


}
