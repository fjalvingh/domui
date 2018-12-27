package to.etc.domui.subinjector;

import java.lang.reflect.Field;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-12-18.
 */
public class SubPageInjectorException extends RuntimeException {
	private final Field m_field;

	public SubPageInjectorException(Field field, Exception x, String what) {
		super(what, x);
		m_field = field;
	}

	public Field getField() {
		return m_field;
	}
}
