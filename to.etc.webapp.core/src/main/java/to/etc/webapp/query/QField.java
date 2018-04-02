package to.etc.webapp.query;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

	@Nonnull
	final private Class<R> m_rootClass;

	@Nonnull
	final private String m_propertyName;

	//protected QField() {
	//	m_propertyName = "";
	//}

	public QField(@Nonnull Class<R> rootClass, @Nonnull String propertyName) {
		m_rootClass = rootClass;
		m_parent = null;
		m_propertyName = propertyName;
	}

	public QField(@Nonnull Class<R> rootClass, @Nullable QField<R, ? > parent, @Nonnull String propertyName) {
		m_rootClass = rootClass;
		m_parent = parent;
		m_propertyName = propertyName;
	}

	@Nonnull
	final public String getName() {
		QField<R, ?> parent = m_parent;
		if(parent == null)
			return m_propertyName;
		return parent.getName() + "." + m_propertyName;
	}

	@Nonnull
	String getPropertyName() {
		return m_propertyName;
	}

	@Nullable
	QField<R, ? > getParent() {
		return m_parent;
	}

	@Nonnull public Class<R> getRootClass() {
		return m_rootClass;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
