package to.etc.formbuilder.pages;

import java.util.*;

import javax.annotation.*;

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


	private Div m_currentRightContents;

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
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("$js/colorpicker.js"), 100);

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
		right.setCssClass("fb-right");

		List<IFbComponent> componentList = r().getComponentList();
		final ComponentPanel cp = new ComponentPanel(componentList);
		final PropertyPanel pp = new PropertyPanel();

		//-- Create tabpanel structure.
		Div labels = new Div();
		right.add(labels);
		labels.setCssClass("fb-tab-lab");
		renderLabel(labels, "Components", new IClicked<NodeBase>() {
			@Override
			public void clicked(NodeBase clickednode) throws Exception {
				setRight(cp);
			}
		});

		renderLabel(labels, "Properties", new IClicked<NodeBase>() {

			@Override
			public void clicked(NodeBase clickednode) throws Exception {
				setRight(pp);
			}
		});
		Div d = m_currentRightContents = new Div();
		right.add(d);
		d.setCssClass("fb-tab-cont");
		d.add(cp);


//		TabPanel tp = new TabPanel();
//		right.add(tp);
//		Div td = new Div();
//		Div topd = new Div();
//		td.add(topd);
//		td.add(cp);
//
//		tp.add(td, "Components");
//
//		Div propd = new Div();
//		propd.add(pp);
//
//		tp.add(propd, "Properties", false);
//		tp.build();

		m_paint.addSelectionChanged(new ISelectionChanged() {
			@Override
			public void selectionChanged(Set<ComponentInstance> newSelection, Set<ComponentInstance> oldSelection) throws Exception {
				pp.selectionChanged(newSelection);
			}
		});

//		appendCreateJS("WebUI.autoHeightReset('#" + topd.getActualID() + "','#" + cp.getActualID() + "', 0);");
//		appendCreateJS("WebUI.autoHeightReset('#" + propd.getActualID() + "','#" + pp.getActualID() + "', 0);");
//
		appendCreateJS("FormBuilder.create('" + m_paint.getActualID() + "','" + cp.getActualID() + "');");
	}

	private void setRight(@Nonnull NodeContainer nc) {
		m_currentRightContents.removeAllChildren();
		m_currentRightContents.add(nc);
	}

	private void renderLabel(@Nonnull Div labels, @Nonnull String string, @Nonnull IClicked<NodeBase> iClicked) {
		Div lab = new Div();
		labels.add(lab);
		lab.setCssClass("fb-tab-lbl");
		Span txt = new Span(string);
		lab.add(txt);
		lab.setClicked(iClicked);
	}


}
