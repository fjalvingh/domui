package to.etc.domuidemo.pages.test;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class IeCheckBoxInTable extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Div one = new Div();
		add(one);
		Checkbox cb2 = new Checkbox();
		one.add(cb2);
		cb2.setClicked(new IClicked<Checkbox>() {
			@Override
			public void clicked(Checkbox clickednode) throws Exception {
				IeCheckBoxInTable.this.add(new MsgDiv("Checkbox-only: " + clickednode.isChecked()));
			}
		});

		add(new VerticalSpacer(20));


		TBody b = addTable();

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
		add(new MsgDiv("tdclicked " + cb.getValue()));
	}

	protected void checkClicked(Checkbox cb) {
		add(new MsgDiv("checkbox clicked " + cb.getValue()));
	}

}
