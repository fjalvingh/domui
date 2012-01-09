package to.etc.sjit;

/**
 * Lanczos3 filter implementation. This usually gives the best results, but it
 * is the most CPU-intensive.
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
public class Lanczos3Filter extends ResamplerFilter {
	@Override
	public String getName() {
		return "Lanczos3";
	}

	public Lanczos3Filter() {
	}

	private float sinC(float value) {
		if(value == 0.0f)
			return 1.0f;

		value *= Math.PI;
		return (float) Math.sin(value) / value;
	}


	@Override
	public float filter(float value) {
		if(value < 0.0f)
			value = -value;
		if(value < 3.0f)
			return sinC(value) * sinC(value / 3.0f);
		return 0.0f;
	}

	@Override
	public float getWidth() {
		return 3.0f;
	}
}
