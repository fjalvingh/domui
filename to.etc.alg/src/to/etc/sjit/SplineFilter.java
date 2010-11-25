package to.etc.sjit;

/**
 * Spline filter implementation
 *
 * @see ResamplerFilter
 * @see ImageSubsampler
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
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
