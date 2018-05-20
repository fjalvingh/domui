package to.etc.domui.component.image;

import org.eclipse.jdt.annotation.NonNull;

public class Dimension {
	@NonNull
	static public final Dimension ICON = new Dimension(16, 16);

	@NonNull
	static public final Dimension BIGICON = new Dimension(32, 32);

	private final int m_width;

	private final int m_height;

	public Dimension(int width, int height) {
		m_width = width;
		m_height = height;
	}

	public Dimension(@NonNull java.awt.Dimension oldd) {
		m_width = oldd.width;
		m_height = oldd.height;
	}

	public int getHeight() {
		return m_height;
	}

	public int getWidth() {
		return m_width;
	}

	public boolean contains(@NonNull Dimension other) {
		return other.getWidth() <= m_width && other.getHeight() <= m_height;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;

		Dimension dimension = (Dimension) o;

		if(m_height != dimension.m_height)
			return false;
		return m_width == dimension.m_width;
	}

	@Override
	public int hashCode() {
		int result = m_width;
		result = 31 * result + m_height;
		return result;
	}
}
