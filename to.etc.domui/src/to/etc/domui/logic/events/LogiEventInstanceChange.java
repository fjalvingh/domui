package to.etc.domui.logic.events;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.webapp.eventmanager.*;

final public class LogiEventInstanceChange extends LogiEventBase {
	@Nonnull
	final private Object m_instance;

	@Nonnull
	final private ChangeType m_type;

	@Nonnull
	final private Map<String, LogiEventPropertyChange< ? >> m_propertyChangeMap = new HashMap<String, LogiEventPropertyChange< ? >>();

	public LogiEventInstanceChange(@Nonnull ChangeType type, @Nonnull String path, @Nonnull Object instance) {
		super(path);
		m_instance = instance;
		m_type = type;
	}

	public <P> void addChange(@Nonnull LogiEventPropertyChange<P> pc) {
		m_propertyChangeMap.put(pc.getPmm().getName(), pc);
	}

	@Nonnull
	public Object getInstance() {
		return m_instance;
	}

	public boolean isPropertyChanged(@Nonnull String propertyName) {
		return m_propertyChangeMap.containsKey(propertyName);
	}

	public <T> boolean isPropertyChanged(@Nonnull PropertyMetaModel<T> pmm) {
		return m_propertyChangeMap.containsKey(pmm.getName());
	}

	@Nullable
	public LogiEventPropertyChange< ? > findPropertyChange(String propertyName) {
		return m_propertyChangeMap.get(propertyName);
	}

	@Nullable
	public <T> LogiEventPropertyChange<T> findPropertyChange(@Nonnull PropertyMetaModel<T> pmm) {
		return (LogiEventPropertyChange<T>) m_propertyChangeMap.get(pmm.getName());
	}

	@Override
	void dump(@Nonnull Appendable a) throws Exception {
		a.append(getPath());
		a.append(" [instance ").append(MetaManager.identify(m_instance)).append(" ").append(m_type.toString()).append(" ").append(Integer.toString(m_propertyChangeMap.size()))
			.append(" properties]\n");
	}
}
