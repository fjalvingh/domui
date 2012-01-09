package to.etc.sjit;

/**
 * Box filter implementation
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
public class BoxFilter extends ResamplerFilter {
	@Override
	public String getName() {
		return "Box";
	}

	public BoxFilter() {
	}

	// Box filter
	// a.k.a. "Nearest Neighbour" filter
	// anme: I have not been able to get acceptable
	//       results with this filter for subsampling.

	@Override
	public float filter(float value) {
		if(value >= -0.5f && value <= 0.5f)
			return 1.0f;
		else
			return 0.0f;
	}

	@Override
	public float getWidth() {
		return 0.5f;
	}
}
