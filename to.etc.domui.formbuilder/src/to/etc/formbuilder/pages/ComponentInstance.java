package to.etc.formbuilder.pages;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;

/**
 * Wraps some kind of thingy inside the editable form.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 15, 2013
 */
public class ComponentInstance {
	final private IFbComponent m_component;

	@Nonnull
	final private String m_componentId;

	@Nullable
	private NodeBase m_rendered;

	@Nonnull
	final private PageContainer m_pageContainer;

	@Nullable
	private LayoutInstance m_parent;

	ComponentInstance(@Nonnull PageContainer page, @Nonnull String id, @Nonnull IFbComponent component) {
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

	@Nonnull
	public String getId() {
		return m_componentId;
	}

	@Nonnull
	public IFbComponent getComponentType() {
		return m_component;
	}

	@Nonnull
	public NodeBase getRendered() throws Exception {
		NodeBase nc = m_rendered;
		if(null == nc) {
			nc = m_rendered = m_component.createNodeInstance();
		}
		return nc;
	}

	@Nullable
	public Object getPropertyValue(@Nonnull PropertyDefinition pd) throws Exception {
		NodeBase nb = getRendered();
		PropertyMetaModel< ? > pmm = MetaManager.getPropertyMeta(nb.getClass(), pd.getName());
		if(null == pmm)
			return null;
		return pmm.getValue(nb);
	}
}
