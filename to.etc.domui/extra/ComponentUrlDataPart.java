package to.etc.domui.parts;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.state.*;
import to.etc.domui.trouble.*;

public class ComponentUrlDataPart implements IUnbufferedPartFactory {
	@Override
	public void generate(@Nonnull DomApplication app, @Nonnull String rurl, @Nonnull RequestContextImpl param) throws Exception {
		//-- Bugfix: Tomcat 7 does not properly remove ;jsessionid from the URL. So let's do it here. It's wrong ofc because we're not supposed to know that is the way sessions are passed.
		int jsid = rurl.toLowerCase().indexOf(";jsessionid=");
		if(jsid != -1) {
			rurl = rurl.substring(0, jsid);									// Remove ;jsessionid and all after.
		}

		//-- Unstring the pathname, in the format: cid/class/componentid/type
		String[] args = rurl.split("/");
		if(args.length < 3)
			throw new IllegalStateException("Invalid input URL '" + rurl + "': must be in format cid/pageclass/componentID/resourceType.");
		String cids = args[0];
		String pname = args[1];
		String wid = args[2];

		//-- 1. Get required parameters and retrieve the proper Page
		if(pname.length() == 0)
			throw new IllegalStateException("Missing 'c' parameter (page class name)");
		if(cids.length() == 0)
			throw new IllegalStateException("Missing 'cid' parameter");
		Class< ? extends UrlPage> pageClass = app.loadPageClass(pname);
		Page page = PageMaker.findPageInConversation(param, pageClass, cids);
		if(page == null)
			throw new ThingyNotFoundException("The page " + pname + " cannot be found in conversation " + cids);

		//-- Locate the component
		if(wid == null)
			throw new IllegalStateException("Missing 'id' parameter");
		NodeBase component = page.findNodeByID(wid);
		if(component == null)
			throw new ThingyNotFoundException("The component " + wid + " on page " + pname + " cannot be found in conversation " + cids);
		IComponentUrlDataProvider be = (IComponentUrlDataProvider) component;
		be.provideUrlData(param);
	}

}
