package to.etc.domui.logic;

import javax.annotation.*;

public class LogiEventBase {
	@Nonnull
	final private String m_path;

	public LogiEventBase(@Nonnull String path) {
		m_path = path;
	}

	@Nonnull
	final public String getPath() {
		return m_path;
	}

}
