package to.etc.domuidemo.components;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.misc.ALink;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.misc.WindowParameters;
import to.etc.domui.component2.navigation.BreadCrumb2;
import to.etc.domui.component2.navigation.BreadCrumb2.IItem;
import to.etc.domui.component2.navigation.BreadCrumb2.Item;
import to.etc.domui.dom.html.ATag;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.DomApplication;
import to.etc.domui.state.IShelvedEntry;
import to.etc.domui.state.PageParameters;
import to.etc.domui.state.ShelvedDomUIPage;
import to.etc.domui.state.UIGoto;
import to.etc.domuidemo.sourceviewer.SourcePage;

import java.util.List;

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

		List<IItem> stack = BreadCrumb2.getPageStacktems("Home");
		List<IShelvedEntry> ps = getPage().getConversation().getWindowSession().getShelvedPageStack();
		if(ps.size() > 1) {                                    // Nothing to go back to (only myself is on page) -> exit
			IShelvedEntry se = ps.get(ps.size() - 2);        // Get the page before me
			if(se instanceof ShelvedDomUIPage) {
				String name = ((ShelvedDomUIPage) se).getPage().getBody().getClass().getName();
				Class<? extends UrlPage> rootPage = DomApplication.get().getRootPage();
				if(rootPage == null || !name.equals(rootPage.getName())) {
					stack.add(0, new Item(new FaIcon(FaIcon.faArrowCircleLeft), "", "Back to the previous screen", iItem -> UIGoto.back()));
				}
			}
		}

		Div d = new Div("d-sbc-crumb");
		add(d);
		d.add(new BreadCrumb2(stack));

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
