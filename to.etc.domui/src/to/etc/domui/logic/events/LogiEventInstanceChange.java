package to.etc.domui.logic.events;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;

final public class LogiEventInstanceChange extends LogiEventBase {
	@Nonnull
	final private Object m_instance;

	@Nonnull
	final private Map<String, LogiEventPropertyChange< ? >> m_propertyChangeMap = new HashMap<String, LogiEventPropertyChange< ? >>();

	public LogiEventInstanceChange(@Nonnull String path, @Nonnull Object instance) {
		super(path);
		m_instance = instance;
	}

	public <P> void addChange(@Nonnull LogiEventPropertyChange<P> pc) {
		m_propertyChangeMap.put(pc.getPmm().getName(), pc);
	}

	@Nonnull
	public Object getInstance() {
		return m_instance;
	}

	@Override
	void dump(@Nonnull Appendable a) throws Exception {
		a.append(getPath());
		a.append(" [instance ").append(MetaManager.identify(m_instance)).append(" changed: ").append(Integer.toString(m_propertyChangeMap.size())).append(" properties]\n");
	}
}
