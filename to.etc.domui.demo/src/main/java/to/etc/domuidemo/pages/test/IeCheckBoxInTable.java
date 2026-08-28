package to.etc.domuidemo.pages.test;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class IeCheckBoxInTable extends UrlPage {
	private ContentPanel m_cp;

	@Override
	public void createContent() throws Exception {
		ContentPanel cp = m_cp = new ContentPanel();
		add(cp);

		Div one = new Div();
		cp.add(one);
		Checkbox cb2 = new Checkbox();
		one.add(cb2);
		cb2.setClicked(new IClicked<Checkbox>() {
			@Override
			public void clicked(Checkbox clickednode) throws Exception {
				m_cp.add(new MsgDiv("Checkbox-only: " + clickednode.isChecked()));
			}
		});

		cp.add(new VerticalSpacer(20));

		TBody b = cp.addTable();

		TD cell = b.addRowAndCell();
		final Checkbox cb = new Checkbox();
		cell.add(cb);
		cell.setBorder(1, "red", "dotted");
		cell.add(" in cell");
		b.addCell().add("Next cell");

		cb.setClicked(new IClicked<Checkbox>() {
			@Override
			public void clicked(Checkbox clickednode) throws Exception {
				checkClicked(clickednode);
			}
		});

		cell.setClicked(new IClicked<NodeBase>() {
			@Override
			public void clicked(NodeBase clickednode) throws Exception {
				tdClicked(cb);
			}
		});


	}

	protected void tdClicked(Checkbox cb) {
		cb.setValue(!cb.getValue());
		m_cp.add(new MsgDiv("tdclicked " + cb.getValue()));
	}

	protected void checkClicked(Checkbox cb) {
		m_cp.add(new MsgDiv("checkbox clicked " + cb.getValue()));
	}

}
