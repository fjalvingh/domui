package to.etc.formbuilder.pages;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;

/**
 * This is the peer component of the painter representing the paint area.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 15, 2013
 */
public class PaintPanel extends Div {
	private LayoutInstance m_rootLayout;

	public PaintPanel(@Nonnull LayoutInstance rootLayout) {
		m_rootLayout = rootLayout;
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("fd-pp");
	}

	public void webActionDropComponent(@Nonnull RequestContextImpl ctx) throws Exception {
		System.out.println("Drop event: ");
	}
}
