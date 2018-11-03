package to.etc.domuidemo.pages.binding.tut1;

import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.component2.combo.ComboLookup2;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.themes.CssColor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 16-10-18.
 */
final public class BindingTut3 extends UrlPage {
	@Override public void createContent() throws Exception {
		List<CssColor> cssColors = CssColor.calculateColors(16);
		List<String> list = cssColors.stream().map(c -> c.toString()).collect(Collectors.toList());

		ComboLookup2<String> colorC = new ComboLookup2<>(list);
		add(colorC);

		VerticalSpacer vs = new VerticalSpacer(50);
		add(vs);
		vs.setBackgroundColor("#000000");

		colorC.bind().to(vs, "backgroundColor");
		colorC.immediate();
	}
}
