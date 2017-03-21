package to.etc.domuidemo.pages.test;

import to.etc.domui.dom.html.*;

public class IeCheckBoxInTable2 extends UrlPage {
	@Override
	public void createContent() throws Exception {
		TBody b = addTable();

		TD cell = b.addRowAndCell();
		final Checkbox cb = new Checkbox();
		cell.add(cb);
		cell.setBorder(1, "red", "dotted");
		cell.add(" in cell");
		b.addCell().add("Next cell");

		cb.setOnValueChanged(new IValueChanged<Checkbox>() {
			@Override
			public void onValueChanged(Checkbox component) throws Exception {
				checkClicked(component);
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
		cb.setValue(Boolean.valueOf(!cb.getValue().booleanValue()));
		add(new MsgDiv("tdclicked " + cb.getValue()));
	}

	protected void checkClicked(Checkbox cb) {
		add(new MsgDiv("checkbox clicked " + cb.getValue()));
	}

}
