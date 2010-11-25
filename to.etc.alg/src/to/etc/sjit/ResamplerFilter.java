package to.etc.sjit;

/**
 * Base class for all resampler filters.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public abstract class ResamplerFilter {
	public abstract float filter(float value);

	public abstract String getName();

	public abstract float getWidth();

	public float sqr(float value) {
		return value * value;
	}
}
