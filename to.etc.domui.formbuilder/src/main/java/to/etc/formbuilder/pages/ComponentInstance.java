package to.etc.formbuilder.pages;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.html.NodeBase;

/**
 * Wraps some kind of thingy inside the editable form.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 15, 2013
 */
public class ComponentInstance {
	final private IFbComponent m_component;

	@NonNull
	final private String m_componentId;

	@Nullable
	private NodeBase m_rendered;

	@NonNull
	final private PageContainer m_pageContainer;

	@Nullable
	private LayoutInstance m_parent;

	ComponentInstance(@NonNull PageContainer page, @NonNull String id, @NonNull IFbComponent component) {
		m_component = component;
		m_componentId = id;
		m_pageContainer = page;
	}

	public void setParent(@Nullable LayoutInstance parent) {
		m_parent = parent;
	}

	@Nullable
	public LayoutInstance getParent() {
		return m_parent;
	}

	@NonNull
	public String getId() {
		return m_componentId;
	}

	@NonNull
	public IFbComponent getComponentType() {
		return m_component;
	}

	@NonNull
	public NodeBase getRendered() throws Exception {
		NodeBase nc = m_rendered;
		if(null == nc) {
			nc = m_rendered = m_component.createNodeInstance();
		}
		return nc;
	}

	@Nullable
	public Object getPropertyValue(@NonNull PropertyDefinition pd) throws Exception {
		NodeBase nb = getRendered();
		PropertyMetaModel< ? > pmm = MetaManager.getPropertyMeta(nb.getClass(), pd.getName());
		if(null == pmm)
			return null;
		return pmm.getValue(nb);
	}
}
