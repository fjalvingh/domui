package to.etc.formbuilder.pages;

import javax.annotation.*;

import to.etc.domui.component.layout.*;
import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;

/**
 * DomUI form designer.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 30, 2013
 */
public class FormDesigner extends UrlPage {
	private PaintPanel m_paint;

	private FormComponentRegistry m_registry;

	@Nonnull
	private FormComponentRegistry r() {
		return m_registry;
	}

	@Override
	public void createContent() throws Exception {
		m_registry = new FormComponentRegistry();

		getPage().addHeaderContributor(HeaderContributor.loadStylesheet("fd/css/formbuilder.css"), 100);
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("fd/js/formbuilder.js"), 100);

		Div top = new Div();
		add(top);
		top.setCssClass("fb-top");

		top.add("Form Designer");

		Div left = new Div();
		add(left);
		left.setCssClass("fb-left");

		setCssClass("fb-main");
		m_paint = new PaintPanel();
		left.add(m_paint);

		Div right = new Div();
		add(right);
		right.setCssClass("fb-right ui-content");

		TabPanel tp = new TabPanel();
		right.add(tp);
		ComponentPanel cp = new ComponentPanel();
		tp.add(cp, "Components");
		PropertyPanel pp = new PropertyPanel();
		tp.add(pp, "Properties");

		r().getComponentList();
	}

}
