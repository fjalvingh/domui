package to.etc.formbuilder.pages;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * A component on the component panel, for dragging into the ui.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 8, 2013
 */
public class PnlComponent extends Div {
	@Nonnull
	final private IFbComponent m_component;

	public PnlComponent(@Nonnull IFbComponent comp) {
		m_component = comp;
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("fb-pc");
		Div pres = new Div();
		add(pres);
		pres.setCssClass("fb-pc-pres");
		m_component.drawSelector(pres);

		Div label = new Div();
		add(label);
		label.setCssClass("fb-pc-label");
		Div d = new Div();
		label.add(d);
		d.add(new Span(m_component.getShortName()));

		appendCreateJS("window._fb.registerComponentType('" + getActualID() + "','" + m_component.getTypeID() + "');");

	}
}
