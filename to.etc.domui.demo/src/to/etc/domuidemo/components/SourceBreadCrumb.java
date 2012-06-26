package to.etc.domuidemo.components;

import java.util.*;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domuidemo.sourceviewer.*;

public class SourceBreadCrumb extends Div {
	@Override
	public void createContent() throws Exception {
		setCssClass("d-sbc");
		WindowSession cm = UIContext.getRequestContext().getWindowSession();

		//-- Get the application's main page as the base;
		int ct = 0;
		List<ShelvedEntry> stack = cm.getShelvedPageStack();
		if(stack.size() == 0) {
			setDisplay(DisplayType.NONE);
			return;
		} else {
			ShelvedEntry se = stack.get(0);
			if(se.getPage().getBody().getClass() != DomApplication.get().getRootPage()) {
				if(DomApplication.get().getRootPage() != null) {
					addPageLink(0, DomApplication.get().getRootPage(), null, "Demo Index", false);
					ct++;
				}
			}
		}
		setDisplay(null);

		//-- Add logo.
		Div right = new Div();
		add(right);
		right.setCssClass("d-sbc-logo");
		ATag at = new ATag();
		right.add(at);
		at.setHref("http://www.domui.org/");
		at.setTarget("_blank");

		Img img = new Img("img/logo-small.png");
		at.add(img);
		img.setImgBorder(0);

		for(int i = 0; i < stack.size(); i++) {
			boolean last = i + 1 >= stack.size();
			Page p = stack.get(i).getPage();
			String ttl = p.getBody().getTitle();
			if(ttl == null || ttl.length() == 0) {
				ttl = p.getBody().getClass().getName();
				ttl = ttl.substring(ttl.lastIndexOf('.') + 1);
			}
			addPageLink(ct, p.getBody().getClass(), p.getPageParameters(), ttl, last);
			ct++;
		}
	}

	private void addPageLink(int ct, Class< ? extends UrlPage> class1, PageParameters pageParameters, String ttl, boolean last) {
		if(ct > 0) {
			//-- Append the marker,
			Span s = new Span();
			add(s);
			s.setCssClass("d-sbc-m");
			s.add(new TextNode(" \u00bb "));
		}

		//-- Create a LINK or a SPAN
		NodeContainer s;
		if(last) {
			s = new Span();
			s.setCssClass("d-sbc-c");
		} else {
			s = new ALink(class1, pageParameters);
			s.setCssClass("d-sbc-l");
		}
		add(s);
		s.setText(ttl);

		ALink l = new ALink(SourcePage.class, new PageParameters("name", class1.getName().replace('.', '/') + ".java"));
		add(l);
		l.setNewWindowParameters(WindowParameters.createFixed(1024, 768, "src"));
		Img img = new Img("img/java.png");
		l.add(img);
		l.setTitle("Show the source file");

		add("\u00a0\u00a0");
	}
}
