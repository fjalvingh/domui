package to.etc.formbuilder.pages;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers all components on the page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 17, 2013
 */
public class PageContainer {
	private int m_idcounter;

	@NonNull
	final private Map<String, ComponentInstance> m_instanceMap = new HashMap<String, ComponentInstance>();

	@NonNull
	public ComponentInstance createComponent(@NonNull IFbComponent type) {
		if(type instanceof IFbLayout)
			return createLayout((IFbLayout) type);

		String nextid = "C" + (m_idcounter++);
		ComponentInstance ci = new ComponentInstance(this, nextid, type);
		m_instanceMap.put(nextid, ci);
		return ci;
	}

	@NonNull
	public LayoutInstance createLayout(@NonNull IFbLayout type) {
		String nextid = "L" + (m_idcounter++);
		LayoutInstance ci = new LayoutInstance(this, nextid, type);
		m_instanceMap.put(nextid, ci);
		return ci;
	}

	@Nullable
	public ComponentInstance findComponent(@NonNull String id) {
		return m_instanceMap.get(id);
	}

	@NonNull
	public ComponentInstance getComponent(@NonNull String id) {
		ComponentInstance ci = findComponent(id);
		if(null == ci)
			throw new IllegalStateException("Form Component[" + id + "] not found");
		return ci;
	}
}
