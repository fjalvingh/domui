package to.etc.domui.component.layout;

import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;

public class ButtonBar extends Table {
	private boolean m_vertical;

	private TD m_center;

	private TBody m_body;

	private List<NodeBase> m_list = new ArrayList<NodeBase>();

	public ButtonBar() {
		setCssClass("ui-buttonbar");
		setCellSpacing("0");
		setCellPadding("0");
		setTableWidth("100%");
	}

	public ButtonBar(boolean vertical) {
		this();
		m_vertical = vertical;
	}

	@Override
	public void createContent() throws Exception {
		m_body = new TBody();
		add(m_body);
		if(m_vertical)
			createVertical();
		else
			createHorizontal();
		for(NodeBase b : m_list)
			appendObject(b);
	}


	private void appendObject(NodeBase b) {
		if(m_vertical)
			appendVertical(b);
		else
			appendHorizontal(b);
	}

	private void appendHorizontal(NodeBase b) {
		m_center.add(b);
	}

	private void appendVertical(NodeBase b) {
		TD td = m_body.addRowAndCell();
		td.add(b);
	}

	/**
	 * For now: just create a row per button; no top- and botton border row.
	 */
	private void createVertical() {
	}

	/**
	 * Create horizontal presentation: 3 cells for border-left, content, border-right
	 */
	private void createHorizontal() {
		m_body.addRow();
		//		TD td = m_body.addCell();	jal 20100222 Buttons do not properly align when this is present.
		//		td.setCssClass("ui-bb-left");

		m_center = m_body.addCell();
		m_center.setCssClass("ui-bb-middle");
		//
		//		td = m_body.addCell();
		//		td.setCssClass("ui-bb-right");
	}

	public void addButton(NodeBase b) {
		m_list.add(b);
		if(isBuilt())
			appendObject(b);
	}

	/**
	 * Add a normal button.
	 * @param txt
	 * @param icon
	 * @param click
	 * @return
	 */
	public DefaultButton addButton(final String txt, final String icon, final IClicked<DefaultButton> click) {
		DefaultButton b = new DefaultButton(txt, icon, click);
		addButton(b);
		return b;
	}

	public DefaultButton addButton(final String txt, final IClicked<DefaultButton> click) {
		DefaultButton b = new DefaultButton(txt, click);
		addButton(b);
		return b;
	}

	public DefaultButton addBackButton(final String txt, final String icon) {
		DefaultButton b = new DefaultButton(txt, icon, new IClicked<DefaultButton>() {
			@Override
			public void clicked(final DefaultButton bxx) throws Exception {
				UIGoto.back();
			}
		});
		addButton(b);
		return b;
	}

	public DefaultButton addBackButton() {
		return addBackButton("Terug", "THEME/btnCancel.png");
	}

	public DefaultButton addConfirmedButton(final String txt, final String msg, final IClicked<DefaultButton> click) {
		DefaultButton b = MsgBox.areYouSureButton(txt, msg, click);
		addButton(b);
		return b;
	}

	public DefaultButton addConfirmedButton(final String txt, final String icon, final String msg, final IClicked<DefaultButton> click) {
		DefaultButton b = MsgBox.areYouSureButton(txt, icon, msg, click);
		addButton(b);
		return b;
	}

	public LinkButton addLinkButton(final String txt, final String img, final IClicked<LinkButton> click) {
		LinkButton b = new LinkButton(txt, img, click);
		addButton(b);
		return b;
	}

	public LinkButton addConfirmedLinkButton(final String txt, final String img, String msg, final IClicked<LinkButton> click) {
		LinkButton b = MsgBox.areYouSureLinkButton(txt, img, msg, click);
		addButton(b);
		return b;
	}
}
