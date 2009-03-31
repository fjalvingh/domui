package to.etc.domui.component.misc;

import java.util.*;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;

/**
 * Shows the current shelved path has a breadcrumb, and allows the user to move up into that path.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 27, 2008
 */
public class ShelveBreadCrumb extends Div {
	@Override
	public void createContent() throws Exception {
		WindowSession	cm	= PageContext.getRequestContext().getWindowSession();

		//-- Get the application's main page as the base;
		List<ShelvedEntry>	stack = cm.getShelvedPageStack();
		if(stack.size() == 0) {
			setDisplay(DisplayType.NONE);
			return;
		}
		setDisplay(null);

		for(int i = 0; i < stack.size(); i++) {
			Page	p = stack.get(i).getPage();
			
			if(i > 0) {
				//-- Append the marker,
				Span	s	= new Span();
				add(s);
				s.add(new TextNode(" > "));
			}

			//-- Create a LINK or a SPAN
			Span	s	= new Span();
			add(s);
			String ttl = p.getBody().getTitle();
			if(ttl == null || ttl.length() == 0) {
				ttl = p.getBody().getClass().getName();
				ttl = ttl.substring(ttl.lastIndexOf('.')+1);
			}
			
			s.setText(ttl);
		}
	}
}
