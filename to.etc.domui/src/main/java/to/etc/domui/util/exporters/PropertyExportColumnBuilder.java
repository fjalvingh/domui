package to.etc.domui.util.exporters;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public class PropertyExportColumnBuilder {
	private final ClassMetaModel	m_model;

	private final List<PropertyExportColumn<?>> m_list = new ArrayList<>();

	public PropertyExportColumnBuilder(ClassMetaModel model) {
		m_model = model;
	}

	public PropertyExportColumnBuilder(Class<?> clz) {
		m_model = MetaManager.findClassMeta(clz);
	}

	public PropertyExportColumnBuilder add(String property) {
		m_list.add(new PropertyExportColumn<>(m_model.getProperty(property)));
		return this;
	}

	public PropertyExportColumnBuilder add(String property, String label) {
		m_list.add(new PropertyExportColumn<>(m_model.getProperty(property), label));
		return this;
	}

	public List<PropertyExportColumn<?>> build() {
		return m_list;
	}
}
