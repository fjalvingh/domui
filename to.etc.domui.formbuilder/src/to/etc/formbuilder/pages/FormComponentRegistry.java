package to.etc.formbuilder.pages;

import java.util.*;

import javax.annotation.*;

/**
 * This singleton class will collect all DomUI components that are usable inside the form builder.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 8, 2013
 */
final public class FormComponentRegistry {
	static private final FormComponentRegistry m_instance = new FormComponentRegistry();

	private List<IFbComponent> m_componentList = new ArrayList<IFbComponent>();

	@Nonnull
	public static FormComponentRegistry getInstance() {
		return m_instance;
	}

	public synchronized void register(@Nonnull IFbComponent component) {
		List<IFbComponent> compl = new ArrayList<IFbComponent>(m_componentList);
		compl.add(component);
		m_componentList = Collections.unmodifiableList(compl);
	}

	public synchronized List<IFbComponent> getComponentList() {
		return m_componentList;
	}


}
