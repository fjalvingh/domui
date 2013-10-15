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

		//-- Get what component we need to create.


	}
}
