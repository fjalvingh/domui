package to.etc.sjit;

/**
 * Hermite filter implementation
 *
 * @see ResamplerFilter
 * @see ImageSubsampler
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class HermiteFilter extends ResamplerFilter {
	@Override
	public String getName() {
		return "Hermite";
	}

	public HermiteFilter() {
	}

	@Override
	public float filter(float value) {
		// f(t) = 2|t|^3 - 3|t|^2 + 1, -1 <= t <= 1
		if(value < 0.0)
			value = -value;
		if(value < 1.0)
			return (2.0f * value - 3.0f) * sqr(value) + 1.0f;
		else
			return 0.0f;
	}

	@Override
	public float getWidth() {
		return 1.0f;
	}
}
