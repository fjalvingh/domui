/*
 * DomUI Java User Interface library
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
package to.etc.domui.parts;

import java.io.*;

import javax.annotation.*;

import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.image.*;

import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.util.resources.*;

/**
 * Themed SVG generator.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 31, 2009
 */
public class SvgPartFactory implements IBufferedPartFactory, IUrlPart {
	static private class SvgKey {
		private String m_rurl;

		private int m_width, m_height;

		public SvgKey(String rurl, int width, int height) {
			m_rurl = rurl;
			m_width = width;
			m_height = height;
		}

		public String getRurl() {
			return m_rurl;
		}

		public int getWidth() {
			return m_width;
		}

		public int getHeight() {
			return m_height;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + m_height;
			result = prime * result + ((m_rurl == null) ? 0 : m_rurl.hashCode());
			result = prime * result + m_width;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			SvgKey other = (SvgKey) obj;
			if(m_height != other.m_height)
				return false;
			if(m_rurl == null) {
				if(other.m_rurl != null)
					return false;
			} else if(!m_rurl.equals(other.m_rurl))
				return false;
			if(m_width != other.m_width)
				return false;
			return true;
		}
	}


	@Override
	public boolean accepts(@Nonnull String rurl) {
		return rurl.endsWith(".png.svg");
	}

	@Override
	public Object decodeKey(@Nonnull String rurl, @Nonnull IExtendedParameterInfo param) throws Exception {
		int width = PartUtil.getInt(param, "w", -1);
		int height = PartUtil.getInt(param, "h", -1);
		return new SvgKey(rurl, width, height);
	}

	@Override
	public void generate(@Nonnull PartResponse pr, @Nonnull DomApplication da, @Nonnull Object key, @Nonnull IResourceDependencyList rdl) throws Exception {
		SvgKey k = (SvgKey) key;

		//-- 1. Get the input as a theme-replaced resource
		String svg = da.getThemeReplacedString(rdl, k.getRurl());

		//-- 2. Now generate the thingy using the Batik transcoder:
		PNGTranscoder coder = new PNGTranscoder();
		//		coder.addTranscodingHint(PNGTranscoder., null);
		TranscoderInput in = new TranscoderInput(new StringReader(svg));
		TranscoderOutput out = new TranscoderOutput(pr.getOutputStream());

		if(k.getWidth() != -1 && k.getHeight() != -1) {
			coder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, Float.valueOf(k.getWidth()));
			coder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, Float.valueOf(k.getHeight()));
		}

		coder.transcode(in, out);

		if(!da.inDevelopmentMode()) { // Not gotten from WebContent or not in DEBUG mode? Then we may cache!
			pr.setCacheTime(da.getDefaultExpiryTime());
		}
		pr.setMime("image/png");
	}
}
