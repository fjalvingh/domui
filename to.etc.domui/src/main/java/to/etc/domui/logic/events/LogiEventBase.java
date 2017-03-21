package to.etc.domui.logic.events;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.webapp.query.*;

abstract public class LogiEventBase {
	@Nonnull
	final private String m_path;

	public LogiEventBase(@Nonnull String path) {
		m_path = path;
	}

	@Nonnull
	final public String getPath() {
		return m_path;
	}

	abstract void dump(@Nonnull Appendable a) throws Exception;

	@Nonnull
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
