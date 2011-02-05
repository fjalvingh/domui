package to.etc.domui.component.menu;

import java.util.*;

import to.etc.domui.dom.html.*;

/**
 * EXPERIMENTAL, INCOMPLETE A popup menu.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 5, 2011
 */
public class PopupMenu extends Div {
	private Object m_targetObject;

	private String m_menuTitle;

	private List<IUIAction< ? >> m_actionList = new ArrayList<IUIAction< ? >>();

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-pmnu");
		Div ttl = new Div();
		add(ttl);
		ttl.setCssClass("pmnu-ttl");
		ttl.add(m_menuTitle == null ? "Menu" : m_menuTitle);

		for(IUIAction< ? > a : m_actionList) {
			if(a.accepts(m_targetObject)) {
				addChoice(a);
			}
		}
	}

	private void addChoice(IUIAction< ? > action) {
		Div d = new Div();
		add(d);
		d.setCssClass("ui-pmnu-action");
		String icon = action.getIcon(m_targetObject);
		if(null != icon) {
			d.setBackgroundImage(icon);
		}
		d.add(action.getName(m_targetObject));


	}

}
