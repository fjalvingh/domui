package to.etc.sjit;

/**
 * Spline filter implementation
 *
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
public class SplineFilter extends ResamplerFilter {
	@Override
	public String getName() {
		return "Spline";
	}

	public SplineFilter() {
	}


	@Override
	public float filter(float value) {
		float tt;
		if(value < 0.0f)
			value = -value;
		if(value < 1.0f) {
			tt = sqr(value);
			return 0.5f * tt * value - tt + 2.0f / 3.0f;
		}
		if(value < 2.0f) {
			value = 2.0f - value;
			return 1.0f / 6.0f * sqr(value) * value;
		}
		return 0.0f;
	}

	@Override
	public float getWidth() {
		return 2.0f;
	}
}
