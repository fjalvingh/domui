package to.etc.formbuilder.pages;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.dom.css.CssBase;
import to.etc.domui.dom.html.IControl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A mutable property definition. This is component-agnostic. Instances are cached and can be checked for referential equality.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 23, 2013
 */
final public class PropertyDefinition {
	@NonNull
	final private Class< ? > m_actualType;

	@NonNull
	final private String m_name;

	@NonNull
	final private String m_category;

	@NonNull
	final private IPropertyEditorFactory m_editor;

	@NonNull
	static private final Map<PropertyDefinition, PropertyDefinition> m_map = new HashMap<PropertyDefinition, PropertyDefinition>();

	static private Map<String, String> m_categoryMap = new HashMap<String, String>();

	private PropertyDefinition(@NonNull Class< ? > actualType, @NonNull String name, @NonNull String category, @NonNull IPropertyEditorFactory editor) {
		m_actualType = actualType;
		m_name = name;
		m_category = category;
		m_editor = editor;
	}

	@NonNull
	public Class< ? > getActualType() {
		return m_actualType;
	}

	@NonNull
	public String getName() {
		return m_name;
	}

	@NonNull
	public String getCategory() {
		return m_category;
	}

	@NonNull
	public IPropertyEditorFactory getEditor() {
		return m_editor;
	}

	@NonNull
	static public PropertyDefinition getDefinition(@NonNull Class< ? > actualType, @NonNull String name, @NonNull String category, @NonNull IPropertyEditorFactory editor) {
		PropertyDefinition pd = new PropertyDefinition(actualType, name, category, editor);
		PropertyDefinition rpd = m_map.get(pd);
		if(rpd != null)
			return rpd;
		m_map.put(pd, pd);
		return pd;
	}

	static synchronized public void registerCategory(@NonNull String category, String... properties) {
		for(String name : properties) {
			m_categoryMap.put(name, category);
		}
	}

	static synchronized public void registerCategories(@NonNull String category, @NonNull Class< ? > ifclss) {
		for(PropertyMetaModel< ? > pmm : MetaManager.findClassMeta(ifclss).getProperties()) {
			if(pmm.getReadOnly() == YesNoType.YES)
				continue;
			if(!m_categoryMap.containsKey(pmm.getName()))
				m_categoryMap.put(pmm.getName(), category);
		}
	}

	@NonNull
	static synchronized public String getCategory(@NonNull String propertyName) {
		String cat = m_categoryMap.get(propertyName);
		return cat == null ? "Miscellaneous" : cat;
	}

	static private final Set<String> m_ignoreSet = new HashSet<String>();

	public static synchronized void ignore(@NonNull String property) {
		m_ignoreSet.add(property);
	}

	public static synchronized boolean isIgnored(@NonNull String property) {
		return m_ignoreSet.contains(property);
	}

	static {
		registerCategories("CSS", CssBase.class);
		registerCategories("Control", IControl.class);

		ignore("componentBundle");
	}
}
