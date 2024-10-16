package to.etc.domui.util.js;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MapScriptScope implements IScriptScope {

	private final Map<String, Object> m_map;

	public MapScriptScope(Map<String, Object> map) {
		m_map = map;
	}

	@Nullable
	@Override
	public <T> T getValue(@NonNull Class<T> valueClass, @NonNull String name) {
		return (T) m_map.get(name);
	}

	@Override
	public <T> void put(@NonNull String name, @Nullable T instance) {
		m_map.put(name, instance);
	}

	@NonNull
	@Override
	public <T> List<T> getProperties(@NonNull Class<T> filterClass) {
		return m_map.values().stream().filter(it -> it.getClass().isAssignableFrom(filterClass)).map(it -> (T) it).collect(Collectors.toList());
	}

	@NonNull
	@Override
	public IScriptScope addObjectProperty(@NonNull String name) {
		throw new IllegalStateException("Not implemented!?");
	}

	@Nullable
	@Override
	public <T> T eval(@NonNull Class<T> targetType, @NonNull Reader r, @NonNull String sourceFileNameIndicator) throws Exception {
		throw new IllegalStateException("Not implemented!?");
	}

	@Nullable
	@Override
	public <T> T eval(@NonNull Class<T> targetType, @NonNull String expression, @NonNull String sourceFileNameIndicator) throws Exception {
		throw new IllegalStateException("Not implemented!?");
	}

	@NonNull
	@Override
	public IScriptScope newScope() {
		throw new IllegalStateException("Not implemented!?");
	}

	@Nullable
	@Override
	public <T> T getAdapter(@NonNull Class<T> clz) {
		return this;
	}
}
