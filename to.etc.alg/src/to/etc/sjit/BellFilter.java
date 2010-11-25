package to.etc.sjit;

/**
 * Implements the Bell filter.
 * @see ResamplerFilter
 * @see ImageSubsampler
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
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
