package to.etc.formbuilder.pages;

import javax.annotation.*;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;

/**
 * This is the peer component of the painter representing the paint area.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 15, 2013
 */
public class PaintPanel extends Div {
	@Nonnull
	final private LayoutInstance m_rootLayout;

	@Nonnull
	final private FormComponentRegistry m_registry;

	public PaintPanel(@Nonnull FormComponentRegistry registry, @Nonnull LayoutInstance rootLayout) {
		m_registry = registry;
		m_rootLayout = rootLayout;
	}

	@Nonnull
	private FormComponentRegistry r() {
		return m_registry;
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("fd-pp");
		renderLayout();


	}

	private void renderLayout() throws Exception {
		renderLayout(this, m_rootLayout);
	}

	private void renderLayout(@Nonnull NodeContainer target, LayoutInstance layout) throws Exception {
		NodeContainer layoutc = layout.getRendered();
		target.add(layoutc);
		updateComponent(layout);

		for(ComponentInstance ci : layout.getComponentList()) {
			if(ci instanceof LayoutInstance) {
				renderLayout(layoutc, (LayoutInstance) ci);
			} else {
				renderComponent(layoutc, ci);
			}
		}
	}

	private void renderComponent(@Nonnull NodeContainer layoutc, @Nonnull ComponentInstance ci) throws Exception {
		NodeBase inst = ci.getRendered();
		layoutc.add(inst);
		updateComponent(ci);
	}

	private void updateComponent(@Nonnull ComponentInstance ci) throws Exception {
		appendJavascript("window._fb.registerInstance('" + ci.getComponentType().getTypeID() + "','" + ci.getRendered().getActualID() + "');");
	}

	public void webActionDropComponent(@Nonnull RequestContextImpl ctx) throws Exception {
		PageParameters pp = PageParameters.createFrom(ctx);
		String type = pp.getString("typeName");
		int x = pp.getInt("x");
		int y = pp.getInt("y");

		IFbComponent component = r().findComponent(type);
		if(null == component) {
			MsgBox.error(this, "Internal: no type '" + type + "'");
			return;
		}
		System.out.println("Drop event: " + component + " @(" + x + "," + y + ")");

		ComponentInstance ci = new ComponentInstance(component);			// Create the instance
		LayoutInstance li = m_rootLayout;

		li.addComponent(ci);
		li.getRendered().add(ci.getRendered());


	}
}
