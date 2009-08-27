package to.etc.sjit;

/**
 * Triangle (linear/bilinear) filter implementation
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
public class TriangleFilter extends ResamplerFilter {
	@Override
	public String getName() {
		return "Triangle";
	}

	public TriangleFilter() {
	}


	// Triangle filter
	// a.k.a. "Linear" or "Bilinear" filter
	@Override
	public float filter(float value) {
		if(value < 0.0f)
			value = -value;
		if(value < 1.0f)
			return 1.0f - value;
		else
			return 0.0f;
	}

	@Override
	public float getWidth() {
		return 1.0f;
	}
}
