package to.etc.domuidemo.pages.overview.input;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.dom.html.*;

public class DemoComboFixed extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);

		List<ValueLabelPair<Integer>> vlist = new ArrayList<ValueLabelPair<Integer>>();
		vlist.add(new ValueLabelPair<Integer>(Integer.valueOf(1), "The number one"));
		vlist.add(new ValueLabelPair<Integer>(Integer.valueOf(2), "The number two"));
		vlist.add(new ValueLabelPair<Integer>(Integer.valueOf(3), "The number three"));
		vlist.add(new ValueLabelPair<Integer>(Integer.valueOf(4), "The number IV"));
		ComboFixed<Integer> combof = new ComboFixed<Integer>(vlist);
		combof.setMandatory(false);
		combof.setValue(Integer.valueOf(3)); // Combo's, like other components, present their properly typed value.

		d.add("ComboBox Fixed using List of value/label pairs");
		d.add(combof);
	}
}
