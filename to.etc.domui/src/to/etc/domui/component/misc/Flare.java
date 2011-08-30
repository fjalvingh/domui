package to.etc.domui.component.misc;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.util.*;

public class Flare extends Div {
	public Flare() {
		setCssClass("ui-flare");
	}

	@Override
	public void createContent() throws Exception {
	}

	/**
	 * Get an instance of a flare for the current request. This creates the instance when
	 * needed and appends it to the layout proper.
	 * @param flareClass
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	static public <T extends Flare> T	get(NodeContainer parent, Class<T> flareClass) {
		//-- 1. Add or get the flare.
		T	f = null;
		NodeContainer nc = parent.getPage().getBody();
		for(NodeBase nb: nc) {
			if(flareClass.isAssignableFrom(nb.getClass())) {
				f = (T) nb;
				break;
			}
		}

		if(null == f) {
			try {
				f = flareClass.newInstance();
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}
			parent.getPage().getBody().add(f);
			f.setDisplay(DisplayType.NONE);
			f.addJavascript();
			parent.getPage().addRemoveAfterRenderNode(f);
		}
		return f;
	}

	protected void	addJavascript() {
		appendJavascript("WebUI.flare('"+getActualID()+"');");
	}


}
