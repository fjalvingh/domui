package to.etc.domui.subinjector;

import to.etc.domui.annotations.UIReinject;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.dom.html.SubPage;
import to.etc.domui.trouble.WikiUrls;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * This is a development time injector for fields of subpages that are
 * not suitable for injection. These fields are checked to make sure they
 * do not contain entity values.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-12-18.
 */
final public class SubFieldEntityCheckingInjector implements ISubPageInjector {
	private final Field m_field;

	public SubFieldEntityCheckingInjector(Field field) {
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

		UIReinject ann = m_field.getAnnotation(UIReinject.class);
		if(null == ann) {
			throw new SubPageInjectorException(m_field, null, "Field " + m_field + " is not annotated with " + UIReinject.class.getSimpleName() + " but does contain an Entity! See " + WikiUrls.SUBPAGES);
		}
		if(Modifier.isFinal(m_field.getModifiers())) {
			throw new SubPageInjectorException(m_field, null, "Field " + m_field + " is final but contains an Entity that needs to be re-injected! See " + WikiUrls.SUBPAGES);
		}

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
