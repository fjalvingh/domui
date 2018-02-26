package to.etc.log;

import javax.annotation.*;

/**
 * Defines self-explanatory logger levels.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
public enum Level {
	TRACE(0), DEBUG(1), INFO(2), WARN(3), ERROR(4), NONE(10);

	final int	m_code;

	Level(int code) {
		m_code = code;
	}

	public int getCode() {
		return m_code;
	}

	public boolean includes(@Nonnull Level level) {
		return m_code <= level.getCode();
	}
}

