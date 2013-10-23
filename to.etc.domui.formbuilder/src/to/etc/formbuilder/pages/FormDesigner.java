package to.etc.formbuilder.pages;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.layout.*;
import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;
import to.etc.formbuilder.pages.PaintPanel.ISelectionChanged;

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
//		m_registry.scanComponents();

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
		m_paint = new PaintPanel(r());
		left.add(m_paint);

		Div right = new Div();
		add(right);
		right.setCssClass("fb-right ui-content");

		TabPanel tp = new TabPanel();
		right.add(tp);
		List<IFbComponent> componentList = r().getComponentList();

		Div td = new Div();
//		td.setHeight("800px");
//		td.setOverflow(Overflow.AUTO);
		Div topd = new Div();
		td.add(topd);
		ComponentPanel cp = new ComponentPanel(componentList);
		td.add(cp);

		tp.add(td, "Components");

		final PropertyPanel pp = new PropertyPanel();
		Div propd = new Div();
		propd.add(pp);

		tp.add(propd, "Properties");
		tp.build();

		m_paint.addSelectionChanged(new ISelectionChanged() {
			@Override
			public void selectionChanged(Set<ComponentInstance> newSelection, Set<ComponentInstance> oldSelection) throws Exception {
				pp.selectionChanged(newSelection);
			}
		});

		appendCreateJS("WebUI.autoHeightReset('#" + topd.getActualID() + "','#" + cp.getActualID() + "', 0);");
		appendCreateJS("WebUI.autoHeightReset('#" + propd.getActualID() + "','#" + pp.getActualID() + "', 0);");

		appendCreateJS("FormBuilder.create('" + m_paint.getActualID() + "','" + cp.getActualID() + "');");
	}


}
