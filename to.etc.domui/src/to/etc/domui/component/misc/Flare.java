package to.etc.domui.component.misc;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.util.*;

public class Flare extends Div {
	/**
	 * If T, flare would stay on screen until any mouse move happens on screen. If F, flare would automatically vanish.
	 */
	private boolean m_autoVanish = true;

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
		if(m_autoVanish) {
			appendJavascript("WebUI.flare('" + getActualID() + "');");
		} else {
			appendJavascript("WebUI.flareStay('" + getActualID() + "');");
		}
	}

	/**
	 * @see Flare#m_autoVanish
	 * @return
	 */
	public boolean isAutoVanish() {
		return m_autoVanish;
	}

	/**
	 * @see Flare#m_autoVanish
	 * @param stayUntilMouseMove
	 */
	public void setAutoVanish(boolean autoVanish) {
		m_autoVanish = autoVanish;
	}
}
