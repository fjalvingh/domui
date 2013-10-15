package to.etc.formbuilder.pages;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * This wraps a component that can contain other components using some kind
 * of layout.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 15, 2013
 */
public class LayoutInstance extends ComponentInstance {
	@Nonnull
	final private IFbLayout m_component;

	private NodeContainer m_rendered;

	@Nonnull
	final private List<ComponentInstance> m_componentList = new ArrayList<ComponentInstance>();

	public LayoutInstance(@Nonnull IFbLayout component) {
		super(component);
		m_component = component;
	}

	@Nonnull
	public List<ComponentInstance> getComponentList() {
		return m_componentList;
	}

	public void addComponent(@Nonnull ComponentInstance ci) {
		m_componentList.add(ci);
	}

	@Override
	@Nonnull
	public NodeContainer getRendered() throws Exception {
		NodeContainer nc = m_rendered;
		if(null == nc) {
			nc = m_rendered = m_component.createNodeInstance();
		}
		return nc;
	}


}
