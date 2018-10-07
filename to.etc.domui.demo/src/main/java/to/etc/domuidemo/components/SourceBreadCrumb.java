package to.etc.domuidemo.components;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.misc.ALink;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.misc.WindowParameters;
import to.etc.domui.component2.navigation.BreadCrumb2;
import to.etc.domui.dom.html.ATag;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Img;
import to.etc.domui.state.PageParameters;
import to.etc.domuidemo.sourceviewer.SourcePage;

final public class SourceBreadCrumb extends Div {
	@Override
	public void createContent() throws Exception {
		setCssClass("d-sbc");

		if(!hasSuper(getPage().getBody(), "WikiExplanationPage")) {
			//-- Add logo.
			ATag at = new ATag();
			add(at);
			at.setHref("http://www.domui.org/");
			at.setTarget("_blank");
			at.setCssClass("d-sbc-logo");

			Img img = new Img("img/logo-small.png");
			at.add(img);
			img.setImgBorder(0);
		}


		Div d = new Div("d-sbc-crumb");
		add(d);
		d.add(BreadCrumb2.createPageCrumb("Home", true));

		ALink l = new ALink(SourcePage.class, new PageParameters("name", getPage().getBody().getClass().getName().replace('.', '/') + ".java"));
		d.add(l);
		l.setNewWindowParameters(WindowParameters.createFixed(1024, 768, "src"));
		l.add(new FaIcon(FaIcon.faCode));
		l.addCssClass("d-sbc-src");
		l.setTitle("Show the source file");

		Div right = new Div("d-sbc-r");
		add(right);

		SmallImgButton refresh = new SmallImgButton("img/reload.png");
		right.add(refresh);
		refresh.setOnClickJS("WebUI.refreshPage();");
		refresh.setTitle("Reload the page fully");

	}

	private static boolean hasSuper(@NonNull Object instance, String what) {
		Class< ? > clz = instance.getClass();
		for(;;) {
			if(clz.getName().endsWith(what)) {
				return true;
			}
			clz = clz.getSuperclass();
			if(clz == null || clz == Object.class)
				return false;
		}
	}
}
