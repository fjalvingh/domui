package to.etc.sjit;

/**
 * Base class for all resampler filters.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public abstract class ResamplerFilter {
	public abstract float filter(float value);

	public abstract String getName();

	public abstract float getWidth();

	public float sqr(float value) {
		return value * value;
	}
}
