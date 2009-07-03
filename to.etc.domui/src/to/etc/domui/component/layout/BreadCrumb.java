package to.etc.domui.component.layout;

import java.util.*;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;

public class BreadCrumb extends Div {
	@Override
	public void createContent() throws Exception {
		setCssClass("ui-brc");
		Table	t	= new Table();
		add(t);
//		t.setCssClass("ui-brc");
		t.setCellSpacing("0");
		t.setCellPadding("0");
		t.setTableWidth("100%");
		TBody	b	= new TBody();
		t.add(b);

		b.addRow();
		TD	td	= b.addCell();
		td.setCssClass("ui-brc-left");
		TD	center	= b.addCell();
		center.setCssClass("ui-brc-middle");
		td	= b.addCell();
		td.setCssClass("ui-brc-right");
		
		WindowSession	cm	= PageContext.getRequestContext().getWindowSession();

		//-- Get the application's main page as the base;
		List<ShelvedEntry>	stack = cm.getShelvedPageStack();
		if(stack.size() == 0) {
			setDisplay(DisplayType.NONE);
			return;
		}
		setDisplay(null);

		for(int i = 0; i < stack.size(); i++) {
			boolean last = i+1 >= stack.size();
			Page	p = stack.get(i).getPage();
			
			if(i > 0) {
				//-- Append the marker,
				Span	s	= new Span();
				center.add(s);
				s.setCssClass("ui-brc-m");
				s.add(new TextNode(" \u00bb "));
			}

			//-- Create a LINK or a SPAN
			NodeContainer	s;
			if(last) {
				s = new Span();
				s.setCssClass("ui-brc-c");
			} else {
				s	= new ALink(p.getBody().getClass(), p.getPageParameters());
				s.setCssClass("ui-brc-l");
			}
			center.add(s);
			String ttl = p.getBody().getLiteralTitle();
			if(ttl == null || ttl.length() == 0) {
				ttl = p.getBody().getClass().getName();
				ttl = ttl.substring(ttl.lastIndexOf('.')+1);
			}
			s.setButtonText(ttl);
		}
	}

}
