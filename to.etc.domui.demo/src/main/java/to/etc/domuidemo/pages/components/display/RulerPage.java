package to.etc.domuidemo.pages.components.display;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.EmbeddedCode;
import to.etc.domui.component.misc.PercentageCompleteRuler2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * PercentageCompleteRuler2 and EmbeddedCode: a bar showing how far something
 * got, and a piece of code shown as code.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class RulerPage extends UrlPage {
	/** How far the first ruler is; a field, because the buttons change it and the page rebuilds. */
	private double m_done = 35.0;

	@Override
	public void createContent() throws Exception {
		setPageTitle("A ruler and a piece of code");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "PercentageCompleteRuler2"));

		PercentageCompleteRuler2 ruler = new PercentageCompleteRuler2();
		ruler.setWidth(300);
		ruler.setValue(m_done);

		PercentageCompleteRuler2 noText = new PercentageCompleteRuler2();
		noText.setWidth(200);
		noText.setShowPercentage(false);
		noText.setValue(m_done);

		PercentageCompleteRuler2 full = new PercentageCompleteRuler2();
		full.setWidth(200);
		full.setValue(100.0);

		PercentageCompleteRuler2 empty = new PercentageCompleteRuler2();
		empty.setWidth(200);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("300 pixels wide").control(ruler);
		fb.label("Without the percentage").control(noText);
		fb.label("At 100%").control(full);
		fb.label("No value at all").control(empty);

		Div buttons = new Div("dm-tut");
		cp.add(buttons);
		buttons.add(new DefaultButton("Less", a -> {
			m_done = Math.max(0, m_done - 15);
			forceRebuild();
		}));
		buttons.add(new DefaultButton("More", a -> {
			m_done = Math.min(100, m_done + 15);
			forceRebuild();
		}));

		cp.add(new Para().add("The value is a percentage between 0 and 100 - anything outside "
			+ "that is clipped rather than refused. The bar also gets a css class naming its "
			+ "rounded value, which is how the theme gives a finished bar its own colour."));

		cp.add(new HTag(1, "EmbeddedCode"));
		cp.add(new EmbeddedCode("SELECT * FROM Album WHERE title ilike '%rock%'"));
		cp.add(new Para().add("EmbeddedCode is a div with one span in it and the class "
			+ "ui-embcd: a piece of code shown as code, in whatever the theme decides code "
			+ "looks like. It shows the text exactly as given - it does not highlight it, "
			+ "and it does not interpret html."));
	}
}
