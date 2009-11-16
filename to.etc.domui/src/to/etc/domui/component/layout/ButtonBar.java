package to.etc.domui.component.layout;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;

public class ButtonBar extends Table {
	private final TD m_center;

	public ButtonBar() {
		setCssClass("ui-buttonbar");
		setCellSpacing("0");
		setCellPadding("0");
		setTableWidth("100%");
		TBody b = new TBody();
		add(b);
		b.addRow();
		TD td = b.addCell();
		//		td.setWidth("15px");
		//		td.setHeight("26px");
		td.setCssClass("ui-bb-left");

		m_center = b.addCell();
		m_center.setCssClass("ui-bb-middle");

		td = b.addCell();
		td.setCssClass("ui-bb-right");
	}

	public DefaultButton addButton(final String txt, final String icon, final IClicked<DefaultButton> click) {
		DefaultButton b = new DefaultButton(txt, icon, click);
		m_center.add(b);
		return b;
	}

	public DefaultButton addButton(final String txt, final IClicked<DefaultButton> click) {
		DefaultButton b = new DefaultButton(txt, click);
		m_center.add(b);
		return b;
	}

	public DefaultButton addBackButton(final String txt, final String icon) {
		DefaultButton b = new DefaultButton(txt, icon, new IClicked<DefaultButton>() {
			public void clicked(final DefaultButton bxx) throws Exception {
				UIGoto.back();
			}
		});
		m_center.add(b);
		return b;
	}

	public DefaultButton addBackButton() {
		return addBackButton("Terug", "THEME/btnCancel.png");
	}

	public NodeContainer getContent() {
		return m_center;
	}
}
