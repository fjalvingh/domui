package to.etc.webapp.query;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Typesafe property base class.
 *
 * @param <R>	The root class type, i.e. the type of the base of the property expression.
 * @param <P>	The type of the field itself.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-4-18.
 */
public class QField<R, P> {
	@Nullable
	final private QField<R, ?> m_parent;

	@NonNull
	final private Class<R> m_rootClass;

	@NonNull
	final private String m_propertyName;

	//protected QField() {
	//	m_propertyName = "";
	//}

	public QField(@NonNull Class<R> rootClass, @NonNull String propertyName) {
		m_rootClass = rootClass;
		m_parent = null;
		m_propertyName = propertyName;
	}

	public QField(@NonNull Class<R> rootClass, @Nullable QField<R, ? > parent, @NonNull String propertyName) {
		m_rootClass = rootClass;
		m_parent = parent;
		m_propertyName = propertyName;
	}

	@NonNull
	final public String getName() {
		QField<R, ?> parent = m_parent;
		if(parent == null)
			return m_propertyName;
		return parent.getName() + "." + m_propertyName;
	}

	@NonNull
	String getPropertyName() {
		return m_propertyName;
	}

	@Nullable
	QField<R, ? > getParent() {
		return m_parent;
	}

	@NonNull public Class<R> getRootClass() {
		return m_rootClass;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
