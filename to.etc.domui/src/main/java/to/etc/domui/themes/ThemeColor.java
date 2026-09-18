package to.etc.domui.themes;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.state.UIContext;

/**
 * Folds a colour that was picked for a light page onto the theme variant the current request
 * renders in.
 *
 * <p>Page code that computes a colour - a palette spread over the hue circle, a heat map, a
 * status colour per row - cannot state that colour in a stylesheet, so it cannot follow a theme
 * variant the way a stylesheet rule does. Almost all such colours are a hue that carries the
 * meaning (green is ok, red is broken, this hue is that source system) over a lightness that only
 * says "this page is white". These methods keep the hue and move the lightness into the band the
 * dark variant paints in; in a light variant, and outside a request altogether, they return their
 * argument unchanged.</p>
 *
 * <p>The three of them say what the colour is <i>for</i>, and that is the one decision per call
 * site:</p>
 * <dl>
 *	<dt>{@link #tint}</dt><dd>a filled area: a row, a cell, a badge, a panel.</dd>
 *	<dt>{@link #ink}</dt><dd>something drawn on the page ground: text, an icon, a rule.</dd>
 *	<dt>{@link #edge}</dt><dd>a border, which has to stay quieter than what it encloses.</dd>
 * </dl>
 *
 * <p>An application that folds the literal colours in its own stylesheets should use the same
 * three names and the same bands there, so that a colour computed in Java and a colour written in
 * scss land in the same place. The bands are stated as constants below; keep the stylesheet's copy
 * of them in step.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
final public class ThemeColor {
	/** A filled area folds into [16%, 48%] lightness, with the palest light colour landing lowest. */
	static private final double TINT_BASE = 0.16;

	static private final double TINT_RANGE = 0.32;

	static private final double TINT_MAX_SATURATION = 0.55;

	/** Text folds into [50%, 85%] lightness, which clears WCAG AA against the grounds tint() produces. */
	static private final double INK_BASE = 0.50;

	static private final double INK_RANGE = 0.35;

	static private final double INK_MAX_SATURATION = 0.80;

	/**
	 * A foreground at least this light was picked to sit on a fill rather than on the page, and
	 * reads on the dark page exactly as it did on the light one - folding it would only darken it.
	 */
	static private final double INK_ALREADY_LIGHT = 0.70;

	/** A border folds into [24%, 42%] lightness: flatter than ink(), or the page looks like a wireframe. */
	static private final double EDGE_BASE = 0.24;

	static private final double EDGE_RANGE = 0.18;

	static private final double EDGE_MAX_SATURATION = 0.40;

	private ThemeColor() {
	}

	/** Set while {@link to.etc.domui.dom.HtmlFileRenderer} builds a standalone document. */
	static private final ThreadLocal<Boolean> m_renderingOffline = new ThreadLocal<>();

	/**
	 * T when the request being rendered uses a variant whose CSS color-scheme is dark. False
	 * outside a request altogether, and false while an offline document is being rendered - see
	 * {@link #setRenderingOffline(boolean)}.
	 */
	static public boolean isDarkScheme() {
		if(Boolean.TRUE.equals(m_renderingOffline.get()))
			return false;
		IRequestContext ctx = UIContext.internalGetContext();
		return null != ctx && "dark".equals(ctx.getThemeVariant().getColorScheme());
	}

	/**
	 * Say that this thread is rendering a standalone document rather than a page, and return what
	 * it was saying before, for the caller to restore.
	 *
	 * <p>{@link to.etc.domui.dom.HtmlFileRenderer} - what a PDF or an html download is made with -
	 * inlines the stylesheet of the theme factory's <i>default</i> variant, not the session's, so
	 * that a report is the same document whoever asks for it. Every colour computed while that
	 * runs has to land on the same variant, or a user whose session is dark would get a light
	 * report with a handful of dark fills in it.</p>
	 */
	static public boolean setRenderingOffline(boolean offline) {
		boolean previous = Boolean.TRUE.equals(m_renderingOffline.get());
		m_renderingOffline.set(offline ? Boolean.TRUE : null);
		return previous;
	}

	/** A filled area: a row, a cell, a badge, a panel. */
	@NonNull
	static public CssColor tint(@NonNull CssColor color) {
		if(!isDarkScheme())
			return color;
		return fold(color, TINT_MAX_SATURATION, TINT_BASE, TINT_RANGE);
	}

	/** Text, an icon, a rule: something drawn on the page ground rather than filling it. */
	@NonNull
	static public CssColor ink(@NonNull CssColor color) {
		if(!isDarkScheme() || color.getHslL() >= INK_ALREADY_LIGHT)
			return color;
		return fold(color, INK_MAX_SATURATION, INK_BASE, INK_RANGE);
	}

	/** A border or other line. */
	@NonNull
	static public CssColor edge(@NonNull CssColor color) {
		if(!isDarkScheme())
			return color;
		return fold(color, EDGE_MAX_SATURATION, EDGE_BASE, EDGE_RANGE);
	}

	/**
	 * The colour moved the fraction passed toward the page ground, which is what "wash this out a
	 * bit" means: toward white on a light page and toward black on a dark one. Code that says
	 * <code>color.lighter(f)</code> means this, and gets it backwards in a dark variant - there,
	 * lighter is away from the ground and so makes the thing louder rather than quieter.
	 */
	@NonNull
	static public CssColor subtler(@NonNull CssColor color, double factor) {
		return isDarkScheme() ? color.darker(factor) : color.lighter(factor);
	}

	/**
	 * {@link #tint(CssColor)} on a CSS value. A value that does not name a colour - null,
	 * "transparent", "inherit", a var() reference - is returned unchanged, because there is
	 * nothing there to fold.
	 */
	@Nullable
	static public String tint(@Nullable String color) {
		CssColor c = foldable(color);
		return null == c ? color : tint(c).toString();
	}

	/** {@link #ink(CssColor)} on a CSS value; see {@link #tint(String)}. */
	@Nullable
	static public String ink(@Nullable String color) {
		CssColor c = foldable(color);
		return null == c ? color : ink(c).toString();
	}

	/** {@link #edge(CssColor)} on a CSS value; see {@link #tint(String)}. */
	@Nullable
	static public String edge(@Nullable String color) {
		CssColor c = foldable(color);
		return null == c ? color : edge(c).toString();
	}

	/**
	 * The colour in the value passed, but only when there is a variant to fold it onto: in a light
	 * variant this returns null so that the caller hands back the value exactly as it came in,
	 * spelling and all.
	 */
	@Nullable
	static private CssColor foldable(@Nullable String color) {
		if(!isDarkScheme())
			return null;
		return CssColor.of(color);
	}

	/**
	 * Keep the hue, cap the saturation - a fully saturated colour is shouting on a dark page - and
	 * mirror the lightness into the band passed, so that the palest colour of a set lands at the
	 * bottom of it and the heaviest at the top. That is what keeps the ordering the light colours
	 * were chosen with: what was the subtler of two stays the subtler of two.
	 */
	@NonNull
	static private CssColor fold(@NonNull CssColor color, double maxSaturation, double base, double range) {
		double saturation = Math.min(color.getHslS(), maxSaturation);
		double lightness = base + (1.0 - color.getHslL()) * range;
		return CssColor.createHSL(color.getHslH(), saturation, lightness);
	}
}
