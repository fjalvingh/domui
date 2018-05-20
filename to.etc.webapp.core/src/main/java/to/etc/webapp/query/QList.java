package to.etc.webapp.query;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.ParameterizedType;

/**
 * Class for creating exists queries based upon the list in the Entity class.
 *
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 * Created on Feb 3, 2013
 */
public class QList<P extends QField<P, ? >, R extends QField<R, ? >> {


	private @NonNull R m_root;

	@NonNull
	QField<P, ? > m_parent;

	@Nullable
	private QExistsSubquery< ? > m_subquery;

	@NonNull
	String m_listName;

	public QList(@NonNull R root, @NonNull QField<P, ? > parent, String listName) throws Exception {
		m_root = root;
		m_parent = parent;
		m_listName = parent.toString().equals("") ? listName : parent.toString() + "." + listName;
	}

	//public @NonNull
	//<T>
	//R exists() throws Exception {
	//
	//	Class<T> rootClass = (Class<T>) getRootClass();
	//	m_subquery = new QExistsSubquery<T>(m_parent.criteria(), rootClass, m_listName);
	//	m_parent.qbrace().add(this);
	//	return m_root;
	//}


	@NonNull Class< ? > getRootClass() {
		return  (Class<?>) ((ParameterizedType) m_root.getClass().getSuperclass().getGenericSuperclass()).getActualTypeArguments()[1];
	}

	@NonNull
	QExistsSubquery< ? > getSubquery() {
		QExistsSubquery< ? > subquery = m_subquery;
		if(null == subquery)
			throw new IllegalStateException("Subquery is not defined");
		return subquery;
	}

	@NonNull
	R getRoot() {
		return m_root;
	}


}
