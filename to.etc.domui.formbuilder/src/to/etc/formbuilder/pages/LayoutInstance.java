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



}
