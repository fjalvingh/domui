package to.etc.formbuilder.pages;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.panellayout.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

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

	public LayoutInstance(@Nonnull PageContainer pc, @Nonnull String id, @Nonnull IFbLayout component) {
		super(pc, id, component);
		m_component = component;
	}

	@Nonnull
	public List<ComponentInstance> getComponentList() {
		return m_componentList;
	}

	public void addComponent(@Nonnull ComponentInstance ci) {
		LayoutInstance oldparent = ci.getParent();
		if(oldparent == this)
			return;
		if(oldparent != null) {
			oldparent.removeComponent(ci);
		}
		m_componentList.add(ci);
		ci.setParent(this);
	}

	public void removeComponent(@Nonnull ComponentInstance ci) {
		if(m_componentList.remove(ci))
			ci.setParent(null);
	}

	@Override
	@Nonnull
	public NodeContainer getRendered() throws Exception {
		NodeContainer nc = m_rendered;
		if(null == nc) {
			nc = m_rendered = m_component.createNodeInstance();
			nc.setMinWidth("200px");
			nc.setMinHeight("200px");
			nc.addCssClass("fb-ui-panel");
		}
		return nc;
	}

	public void positionComponent(ComponentInstance ci, IntPoint point) throws Exception {
		LayoutPanelBase lpb = (LayoutPanelBase) getRendered();
		lpb.getLayout().place(lpb, ci.getRendered(), point);
	}


}
