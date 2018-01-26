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

	public Rect(@Nonnull Point pointa, @Nonnull Point pointb) {
		m_left = pointa.getX();
		m_top = pointa.getY();
		m_right = pointb.getX();
		m_bottom = pointb.getY();
	}

	/**
	 * If needed, return a new rect where size has no negative amounts
	 * @return
	 */
	@Nonnull
	public Rect normalize() {
		if(m_left <= m_right && m_top <= m_bottom)
			return this;
		int l, r;
		if(m_left > m_right) {
			l = m_right;
			r = m_left;
		} else {
			r = m_right;
			l = m_left;
		}
		int t, b;
		if(m_top > m_bottom) {
			t = m_bottom;
			b = m_top;
		} else {
			b = m_bottom;
			t = m_top;
		}
		return new Rect(l, t, r, b);
	}

	@Nonnull
	public final Point getPosition() {
		return new Point(m_left, m_top);
	}

	@Nonnull
	public final Dimension getDimension() {
		return new Dimension(getWidth(), getHeight());
	}

	public int getWidth() {
		return m_right - m_left;
	}

	public int getHeight() {
		return m_bottom - m_top;
	}

	/**
	 * Move the entire rectangle.
	 * @param shiftX
	 * @param shiftY
	 * @return
	 */
	@Nonnull
	public Rect move(int shiftX, int shiftY) {
		return new Rect(m_left + shiftX, m_top + shiftY, m_right + shiftX, m_bottom + shiftY);
	}

	@Override
	public String toString() {
		return "rect ("+m_left+", " + m_top +") to (" + m_right + ", " + m_bottom +"), dims=("+getDimension().getWidth()+", " + getDimension().getHeight()+")";
	}

	/**
	 * Return T if this rect has any kind of intersection with the other rectangle.
	 * @param rect
	 * @return
	 */
	public boolean overlaps(@Nonnull Rect rect) {
		Rect a = normalize();
		Rect b = rect.normalize();

		//-- can X overlap?
		if(a.m_right < b.m_left || a.m_left >= b.m_right)
			return false;

		//-- Can Y overlap?
		return a.m_bottom >= b.m_top && a.m_top < b.m_bottom;
	}
}
