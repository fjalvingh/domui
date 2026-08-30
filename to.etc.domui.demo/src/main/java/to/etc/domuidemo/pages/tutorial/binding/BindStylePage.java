package to.etc.domuidemo.pages.tutorial.binding;

import to.etc.domui.component.binding.StyleBinder;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.combo.ComboFixed2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

/**
 * Tutorial, "data binding", step 5: a StyleBinding maps a model value onto a
 * css class of a node.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class BindStylePage extends UrlPage {
	private final AlbumOrder m_order = new AlbumOrder();

	@Override
	public void createContent() throws Exception {
		setPageTitle("Binding a style");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Binding a style"));

		ComboFixed2<OrderState> stateC = ComboFixed2.createEnumCombo(OrderState.class);
		stateC.bind().to(m_order, AlbumOrder_.state());
		stateC.immediate();

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Order state").control(stateC);

		Div box = new Div("dm-tut");
		cp.add(box);
		box.add("The state of this order decides the colour of this box.");

		new StyleBinder()
			.define(OrderState.New, "dm-tut-new")
			.define(OrderState.Paid, "dm-tut-paid")
			.define(OrderState.Shipped, "dm-tut-shipped")
			.define(OrderState.Cancelled, "dm-tut-cancelled")
			.bind(box).to(m_order, AlbumOrder_.state());
	}
}
