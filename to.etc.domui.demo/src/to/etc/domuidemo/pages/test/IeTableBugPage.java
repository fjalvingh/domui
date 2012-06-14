package to.etc.domuidemo.pages.test;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;

public class IeTableBugPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Table t = new Table();
		t.setTableBorder(2);
		TBody body = t.getBody();
		TR tr = body.addRow();
		final TD a = tr.addCell();
		final TD b = tr.addCell();

		a.setText("aaaaaaaa");
		a.setClicked(new IClicked<NodeBase>() {

			@Override
			public void clicked(NodeBase clickednode) throws Exception {
				TD td = new TD();
				td.setValign(TableVAlign.TOP);
				td.setText("ffffffffff");
				td.setVerticalAlign(VerticalAlignType.TOP); // <<-- This fixes ie7's blooper where it does not see the valign, sigh
				a.replaceWith(td);
			}
		});
		a.setValign(TableVAlign.TOP);
		b.setText("bbbbbbbbbbbbbbbb");
		b.setHeight("100px");
		body.addRow();
		add(t);
	}
}
