package to.etc.domui.logic;

import javax.annotation.*;

public class LogiEventInstanceChange extends LogiEventBase {
	@Nonnull
	final private Object m_instance;

	public LogiEventInstanceChange(@Nonnull String path, @Nonnull Object instance) {
		super(path);
		m_instance = instance;
	}


}
