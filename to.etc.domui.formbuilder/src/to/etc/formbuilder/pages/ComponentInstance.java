package to.etc.formbuilder.pages;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * Wraps some kind of thingy inside the editable form.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 15, 2013
 */
public class ComponentInstance {
	final private IFbComponent m_component;

	private NodeBase m_rendered;

	public ComponentInstance(@Nonnull IFbComponent component) {
		m_component = component;
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

}
