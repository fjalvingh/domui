package to.etc.domui.component.image;

import javax.annotation.*;

public class Dimension {
	private final int m_width;

	private final int m_height;

	public Dimension(int width, int height) {
		m_width = width;
		m_height = height;
	}

	public int getHeight() {
		return m_height;
	}

	public int getWidth() {
		return m_width;
	}

	public boolean contains(@Nonnull Dimension other) {
		return other.getWidth() <= m_width && other.getHeight() <= m_height;
	}
}
