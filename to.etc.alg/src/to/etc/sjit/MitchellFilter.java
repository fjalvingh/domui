package to.etc.sjit;

/**
 * Mitchell filter implementation
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
public class MitchellFilter extends ResamplerFilter {
	@Override
	public String getName() {
		return "Mitchell";
	}

	public MitchellFilter() {
	}

	static private final float	B	= (1.0f / 3.0f);

	static private final float	C	= (1.0f / 3.0f);


	@Override
	public float filter(float value) {
		float tt;
		if(value < 0.0f)
			value = -value;
		tt = sqr(value);
		if(value < 1.0f) {
			value = (((12.0f - 9.0f * B - 6.0f * C) * (value * tt)) + ((-18.0f + 12.0f * B + 6.0f * C) * tt) + (6.0f - 2f * B));
			return value / 6.0f;
		}
		if(value < 2.0f) {
			value = (((-1.0f * B - 6.0f * C) * (value * tt)) + ((6.0f * B + 30.0f * C) * tt) + ((-12.0f * B - 48.0f * C) * value) + (8.0f * B + 24f * C));
			return value / 6.0f;
		}
		return 0.0f;
	}

	@Override
	public float getWidth() {
		return 2.0f;
	}
}
