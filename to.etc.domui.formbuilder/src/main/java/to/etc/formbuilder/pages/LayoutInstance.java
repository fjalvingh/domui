package to.etc.formbuilder.pages;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.panellayout.LayoutPanelBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.util.IntPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * This wraps a component that can contain other components using some kind
 * of layout.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 15, 2013
 */
public class LayoutInstance extends ComponentInstance {
	@NonNull
	final private IFbLayout m_component;

	private NodeContainer m_rendered;

	@NonNull
	final private List<ComponentInstance> m_componentList = new ArrayList<ComponentInstance>();

	public LayoutInstance(@NonNull PageContainer pc, @NonNull String id, @NonNull IFbLayout component) {
		super(pc, id, component);
		m_component = component;
	}

	@NonNull
	public List<ComponentInstance> getComponentList() {
		return m_componentList;
	}

	public void addComponent(@NonNull ComponentInstance ci) {
		LayoutInstance oldparent = ci.getParent();
		if(oldparent == this)
			return;
		if(oldparent != null) {
			oldparent.removeComponent(ci);
		}
		m_componentList.add(ci);
		ci.setParent(this);
	}

	public void removeComponent(@NonNull ComponentInstance ci) {
		if(m_componentList.remove(ci))
			ci.setParent(null);
	}

	@Override
	@NonNull
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
