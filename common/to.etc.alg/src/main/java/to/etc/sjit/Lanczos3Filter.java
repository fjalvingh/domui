/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.sjit;

/**
 * Lanczos3 filter implementation. This usually gives the best results, but it
 * is the most CPU-intensive.
 *
 * @see ResamplerFilter
 * @see ImageSubsampler
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
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

		value *= (float) Math.PI;
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
