package to.etc.domuidemo.pages.components.input;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.graph.ColorPicker;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * ColorPicker: the picker itself, always open on the page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ColorPickerPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("ColorPicker: the open picker");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "ColorPicker: the open picker"));

		ColorPicker picker = new ColorPicker();
		picker.setValue("c05a2a");
		cp.add(picker);

		Div shown = new Div("dm-tut");
		shown.add("Pick a colour, then press the button.");

		cp.add(new DefaultButton("What did I pick?", a -> {
			String colour = picker.getValue();
			shown.removeAllChildren();
			shown.add("The picker holds " + colour + ": ");
			Div swatch = new Div();
			swatch.setBackgroundColor("#" + colour);
			swatch.setWidth("60px");
			swatch.setHeight("20px");
			swatch.setDisplay(to.etc.domui.dom.css.DisplayType.INLINE_BLOCK);
			shown.add(swatch);
		}));

		cp.add(new DefaultButton("Set it to DomUI blue", a -> picker.setValue("4a7ebb")));
		cp.add(shown);

		cp.add(new Para().add("The picker writes every change straight into a hidden input, "
			+ "so the value is on the server by the time any next request arrives - the "
			+ "button above does not have to ask the browser for it."));
	}
}
