package to.etc.sjit;

/**
 * Implements the Bell filter.
 * @see ResamplerFilter
 * @see ImageSubsampler
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class BellFilter extends ResamplerFilter {
	@Override
	public String getName() {
		return "Bell";
	}

	public BellFilter() {
	}

	@Override
	public float filter(float value) {
		if(value < 0.0f)
			value = -value;
		if(value < 0.5f)
			return 0.75f - sqr(value);
		if(value < 1.5f)
			return 0.5f * sqr(value - 1.5f);
		return 0.0f;
	}

	@Override
	public float getWidth() {
		return 1.5f;
	}
}
