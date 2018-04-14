package to.etc.domui.logic.events;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.MetaManager;
import to.etc.webapp.query.IIdentifyable;

abstract public class LogiEventBase {
	@NonNull
	final private String m_path;

	public LogiEventBase(@NonNull String path) {
		m_path = path;
	}

	@NonNull
	final public String getPath() {
		return m_path;
	}

	abstract void dump(@NonNull Appendable a) throws Exception;

	@NonNull
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		try {
			dump(sb);
			return sb.toString();
		} catch(Exception x) {
			return x.toString();
		}
	}

	protected <T> String toString(@Nullable T value) {
		if(null == value)
			return "null";
		if(value instanceof IIdentifyable) {
			return MetaManager.identify(value);
		}

		String s = String.valueOf(value);
		if(s.length() > 80)
			return s.substring(0, 80) + "...";
		return s;
	}

}
