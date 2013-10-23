package to.etc.formbuilder.pages;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;

/**
 * A mutable property definition. This is component-agnostic. Instances are cached and can be checked for referential equality.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 23, 2013
 */
public class PropertyDefinition {
	@Nonnull
	final private Class< ? > m_actualType;

	@Nonnull
	final private String m_name;

	@Nonnull
	final private String m_category;

	@Nonnull
	final private IPropertyEditor m_editor;

	@Nonnull
	static private final Map<PropertyDefinition, PropertyDefinition> m_map = new HashMap<PropertyDefinition, PropertyDefinition>();

	static private Map<String, String> m_categoryMap = new HashMap<String, String>();

	private PropertyDefinition(@Nonnull Class< ? > actualType, @Nonnull String name, @Nonnull String category, @Nonnull IPropertyEditor editor) {
		m_actualType = actualType;
		m_name = name;
		m_category = category;
		m_editor = editor;
	}

	@Nonnull
	public Class< ? > getActualType() {
		return m_actualType;
	}

	@Nonnull
	public String getName() {
		return m_name;
	}

	@Nonnull
	public String getCategory() {
		return m_category;
	}

	@Nonnull
	public IPropertyEditor getEditor() {
		return m_editor;
	}

	@Nonnull
	static public PropertyDefinition getDefinition(@Nonnull Class< ? > actualType, @Nonnull String name, @Nonnull String category, @Nonnull IPropertyEditor editor) {
		PropertyDefinition pd = new PropertyDefinition(actualType, name, category, editor);
		PropertyDefinition rpd = m_map.get(pd);
		if(rpd != null)
			return rpd;
		m_map.put(pd, pd);
		return pd;
	}

	static synchronized public void registerCategory(@Nonnull String category, String... properties) {
		for(String name : properties) {
			m_categoryMap.put(name, category);
		}
	}

	static synchronized public void registerCategories(@Nonnull String category, @Nonnull Class< ? > ifclss) {
		for(PropertyMetaModel< ? > pmm : MetaManager.findClassMeta(ifclss).getProperties()) {
			if(pmm.getReadOnly() == YesNoType.YES)
				continue;
			m_categoryMap.put(pmm.getName(), category);
		}
	}

	@Nonnull
	static synchronized public String getCategory(@Nonnull String propertyName) {
		String cat = m_categoryMap.get(propertyName);
		return cat == null ? "Miscellaneous" : cat;
	}
}
