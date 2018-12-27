package to.etc.domui.subinjector;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.dom.html.SubPage;

import java.lang.reflect.Field;

/**
 * This checks the value of a specific field, and if it contains some kind
 * of entity it gets reloaded in the subpage's session. This version is a
 * non-checking one: it knows the field should be reinjected if it contains
 * something.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-12-18.
 */
@NonNullByDefault
final public class SubFieldEntityInjector implements ISubPageInjector {
	private final Field m_field;

	public SubFieldEntityInjector(Field field) {
		m_field = field;
	}

	@Override public void inject(SubPage page) throws Exception {
		m_field.setAccessible(true);
		Object fieldValue;
		try {
			fieldValue = m_field.get(page);
		} catch(Exception x) {
			throw new SubPageInjectorException(m_field, x, "Failed to get value from " + m_field + ": " + x.getMessage());
		}

		//-- Is this a persistent class?
		if(fieldValue == null)
			return;
		ClassMetaModel classMeta = MetaManager.findClassMeta(fieldValue.getClass());
		if(! classMeta.isPersistentClass())
			return;

		//-- Reload the value
		Object newValue;
		try {
			newValue = page.getSharedContext().reload(fieldValue);
		} catch(Exception x) {
			throw new SubPageInjectorException(m_field, x, "Failed to reload instance from field " + m_field
				+ "\nInstance: " + fieldValue
				+ "\nException: " + x
			);
		}
		try {
			m_field.set(page, newValue);
		} catch(Exception x) {
			throw new SubPageInjectorException(m_field, x, page.getClass().getName() + " ERROR Failed to inject " + m_field + " with " + newValue + ": " + x);
		}
		if(SubPageInjector.LOG.isInfoEnabled()) {
			SubPageInjector.LOG.info("Injected " + newValue + "@" + System.identityHashCode(newValue) + " into " + m_field + " (from " + System.identityHashCode(fieldValue) + ")");
		}
	}
}
