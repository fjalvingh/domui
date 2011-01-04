package to.etc.domui.component.graph;

import to.etc.domui.dom.html.*;

/**
 * This uses the code from http://nofunc.org/DHTML_Color_Picker/, with many thanks. The
 * original code has the <a href="http://creativecommons.org/publicdomain/zero/1.0/">CC0</a> license,
 * shich should allow for redistribution into a LGPLed application.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 4, 2011
 */
public class ColorPicker extends Div {
	/**
	 * Create the required structure.
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		add(new Div("DOES NOT WORK YET"));
		setOnMouseDownJS("WebUI.Picker.HSVslide('drag','" + getActualID() + "',event)");

		setCssClass("ui-cpk-plugin");
		Div cur = new Div();
		add(cur);
		cur.setCssClass("ui-cpk-plugcur");

		Div plughex = new Div();
		add(plughex);
		plughex.setCssClass("ui-cpk-plughex");
		plughex.add("FFFFFF");
		plughex.setOnMouseDownJS("WebUI.Picker._stop=0; setTimeout('WebUI.Picker._stop=1', 100);");

		Div plugclose = new Div();
		add(plugclose);
		plugclose.setCssClass("ui-cpk-plugclose");
		plugclose.add("X");
		add(new BR());

		Div sv = new Div();
		add(sv);
		sv.setCssClass("ui-cpk-sv");
		sv.setTitle("Saturation + Value");
		sv.setOnMouseDownJS("WebUI.Picker.HSVslide('SVslide','" + getActualID() + "',event)");

		Div svslide = new Div();
		sv.add(svslide);
		svslide.setCssClass("ui-cpk-svslide");
		svslide.add(new BR());

		Form f = new Form();
		add(f);
		f.setCssClass("ui-cpk-h");
		f.setTitle("Hue");
		f.setOnMouseDownJS("WebUI.Picker.HSVslide('Hslide','" + getActualID() + "',event)");

		Div hslide = new Div();
		f.add(hslide);
		hslide.setCssClass("ui-cpk-hslide");
		hslide.add(new BR());

		Div hmodel = new Div();
		f.add(hmodel);
		hmodel.setCssClass("ui-cpk-hmodel");
	}
}
