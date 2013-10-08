package to.etc.formbuilder.pages;

import to.etc.domui.dom.html.*;

public class AutoComponent implements IFbComponent {
	final private Class< ? extends NodeBase> m_componentClass;

	public AutoComponent(Class< ? extends NodeBase> componentClass) {
		m_componentClass = componentClass;
	}


}
