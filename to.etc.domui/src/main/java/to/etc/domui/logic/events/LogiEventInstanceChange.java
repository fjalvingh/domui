package to.etc.domui.logic.events;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.webapp.eventmanager.ChangeType;

import java.util.HashMap;
import java.util.Map;

final public class LogiEventInstanceChange extends LogiEventBase {
	@NonNull
	final private Object m_instance;

	@NonNull
	final private ChangeType m_type;

	@NonNull
	final private Map<String, LogiEventPropertyChange< ? >> m_propertyChangeMap = new HashMap<String, LogiEventPropertyChange< ? >>();

	public LogiEventInstanceChange(@NonNull ChangeType type, @NonNull String path, @NonNull Object instance) {
		super(path);
		m_instance = instance;
		m_type = type;
	}

	public <P> void addChange(@NonNull LogiEventPropertyChange<P> pc) {
		m_propertyChangeMap.put(pc.getPmm().getName(), pc);
	}

	@NonNull
	public Object getInstance() {
		return m_instance;
	}

	public boolean isPropertyChanged(@NonNull String propertyName) {
		return m_propertyChangeMap.containsKey(propertyName);
	}

	public <T> boolean isPropertyChanged(@NonNull PropertyMetaModel<T> pmm) {
		return m_propertyChangeMap.containsKey(pmm.getName());
	}

	@Nullable
	public LogiEventPropertyChange< ? > findPropertyChange(String propertyName) {
		return m_propertyChangeMap.get(propertyName);
	}

	@Nullable
	public <T> LogiEventPropertyChange<T> findPropertyChange(@NonNull PropertyMetaModel<T> pmm) {
		return (LogiEventPropertyChange<T>) m_propertyChangeMap.get(pmm.getName());
	}

	@Override
	void dump(@NonNull Appendable a) throws Exception {
		a.append(getPath());
		a.append(" [instance ").append(MetaManager.identify(m_instance)).append(" ").append(m_type.toString()).append(" ").append(Integer.toString(m_propertyChangeMap.size()))
			.append(" properties]\n");
	}
}
