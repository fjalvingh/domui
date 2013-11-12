package to.etc.formbuilder.pages;

import java.util.*;

import javax.annotation.*;

/**
 * Registers all components on the page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 17, 2013
 */
public class PageContainer {
	private int m_idcounter;

	@Nonnull
	final private Map<String, ComponentInstance> m_instanceMap = new HashMap<String, ComponentInstance>();

	@Nonnull
	public ComponentInstance createComponent(@Nonnull IFbComponent type) {
		if(type instanceof IFbLayout)
			return createLayout((IFbLayout) type);

		String nextid = "C" + (m_idcounter++);
		ComponentInstance ci = new ComponentInstance(this, nextid, type);
		m_instanceMap.put(nextid, ci);
		return ci;
	}

	@Nonnull
	public LayoutInstance createLayout(@Nonnull IFbLayout type) {
		String nextid = "L" + (m_idcounter++);
		LayoutInstance ci = new LayoutInstance(this, nextid, type);
		m_instanceMap.put(nextid, ci);
		return ci;
	}

	@Nullable
	public ComponentInstance findComponent(@Nonnull String id) {
		return m_instanceMap.get(id);
	}

	@Nonnull
	public ComponentInstance getComponent(@Nonnull String id) {
		ComponentInstance ci = findComponent(id);
		if(null == ci)
			throw new IllegalStateException("Form Component[" + id + "] not found");
		return ci;
	}
}
