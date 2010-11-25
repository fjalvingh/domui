package to.etc.sjit;

/**
 * Triangle (linear/bilinear) filter implementation
 *
 * @see ResamplerFilter
 * @see ImageSubsampler
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
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
