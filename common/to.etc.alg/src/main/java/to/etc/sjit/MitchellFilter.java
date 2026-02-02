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
 * Mitchell filter implementation
 *
 * @see ResamplerFilter
 * @see ImageSubsampler
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class MitchellFilter extends ResamplerFilter {
	@Override
	public String getName() {
		return "Mitchell";
	}

	public MitchellFilter() {
	}

	static private final float	B	= (1.0f / 3.0f);

	static private final float	C	= (1.0f / 3.0f);


	@Override
	public float filter(float value) {
		float tt;
		if(value < 0.0f)
			value = -value;
		tt = sqr(value);
		if(value < 1.0f) {
			value = (((12.0f - 9.0f * B - 6.0f * C) * (value * tt)) + ((-18.0f + 12.0f * B + 6.0f * C) * tt) + (6.0f - 2f * B));
			return value / 6.0f;
		}
		if(value < 2.0f) {
			value = (((-1.0f * B - 6.0f * C) * (value * tt)) + ((6.0f * B + 30.0f * C) * tt) + ((-12.0f * B - 48.0f * C) * value) + (8.0f * B + 24f * C));
			return value / 6.0f;
		}
		return 0.0f;
	}

	@Override
	public float getWidth() {
		return 2.0f;
	}
}
