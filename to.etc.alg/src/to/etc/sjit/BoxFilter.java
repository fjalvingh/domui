package to.etc.sjit;

/**
 * Box filter implementation
 *
 * @see ResamplerFilter
 * @see ImageSubsampler
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
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
