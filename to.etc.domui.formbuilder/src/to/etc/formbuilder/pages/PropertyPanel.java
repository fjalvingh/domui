package to.etc.formbuilder.pages;

import java.util.*;

import to.etc.domui.dom.html.*;

/**
 * Shows and allows editing of the currently-selected component instance(s).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 8, 2013
 */
public class PropertyPanel extends Div {
	private Set<ComponentInstance> m_selected = new HashSet<ComponentInstance>();

	@Override
	public void createContent() throws Exception {
		// TODO Auto-generated method stub
		super.createContent();
	}

	public void selectionChanged(Set<ComponentInstance> newSelection) {
		if(newSelection.size() == 0) {
			m_selected.clear();
		} else if(newSelection.size() == 1) {
			m_selected = newSelection;
		}
		forceRebuild();
	}


}
