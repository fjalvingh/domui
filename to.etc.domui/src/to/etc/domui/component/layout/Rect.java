package to.etc.domui.component.layout;

import javax.annotation.*;

import to.etc.domui.component.image.*;

/**
 * Represents immutable 2d bounds, position (offset) and dimension (size).
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Jan 27, 2015
 */
public final class Rect {
	private final int m_left;

	private final int m_top;

	private final int m_right;

	private final int m_bottom;

	public final int getLeft() {
		return m_left;
	}

	public final int getTop() {
		return m_top;
	}

	public final int getRight() {
		return m_right;
	}

	public final int getBottom() {
		return m_bottom;
	}

	public Rect(int left, int top, int right, int bottom) {
		m_left = left;
		m_top = top;
		m_right = right;
		m_bottom = bottom;
	}

	public Rect(@Nonnull Point position, @Nonnull Dimension size) {
		m_left = position.getX();
		m_top = position.getY();
		m_right = m_left + size.getWidth();
		m_bottom = m_top + size.getHeight();
	}

	@Nonnull
	public final Point getPosition() {
		return new Point(m_left, m_top);
	}

	@Nonnull
	public final Dimension getDimension() {
		return new Dimension(m_right - m_left, m_bottom - m_top);
	}

}
