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

import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import to.etc.domui.parts.SvgPartFactory.SvgKey;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IExtendedParameterInfo;
import to.etc.domui.server.IParameterInfo;
import to.etc.domui.server.parts.IBufferedPartFactory;
import to.etc.domui.server.parts.IUrlMatcher;
import to.etc.domui.server.parts.PartResponse;
import to.etc.domui.util.resources.IResourceDependencyList;

import javax.annotation.Nonnull;
import java.io.StringReader;
import java.util.Objects;

/**
 * Themed SVG generator.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 31, 2009
 */
public class SvgPartFactory implements IBufferedPartFactory<SvgKey> {
	static public final IUrlMatcher MATCHER = new IUrlMatcher() {
		@Override public boolean accepts(@Nonnull IParameterInfo parameters) {
			return parameters.getInputPath().endsWith(".png.svg");		}
	};

	static public final class SvgKey {
		private final String m_theme;

		private String m_rurl;

		private int m_width, m_height;

		public SvgKey(String theme, String rurl, int width, int height) {
			m_theme = theme;
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

		public String getTheme() {
			return m_theme;
		}

		@Override public boolean equals(Object o) {
			if(this == o)
				return true;
			if(o == null || getClass() != o.getClass())
				return false;
			SvgKey svgKey = (SvgKey) o;
			return m_width == svgKey.m_width &&
				m_height == svgKey.m_height &&
				Objects.equals(m_theme, svgKey.m_theme) &&
				Objects.equals(m_rurl, svgKey.m_rurl);
		}

		@Override public int hashCode() {
			return Objects.hash(m_theme, m_rurl, m_width, m_height);
		}
	}

	@Override
	public @Nonnull SvgKey decodeKey(DomApplication application, @Nonnull IExtendedParameterInfo param) throws Exception {
		int width = PartUtil.getInt(param, "w", -1);
		int height = PartUtil.getInt(param, "h", -1);
		return new SvgKey(param.getThemeName(), param.getInputPath(), width, height);
	}

	@Override
	public void generate(@Nonnull PartResponse pr, @Nonnull DomApplication da, @Nonnull SvgKey k, @Nonnull IResourceDependencyList rdl) throws Exception {
		//-- 1. Get the input as a theme-replaced resource
		String svg = da.internalGetThemeManager().getThemeReplacedString(rdl, k.getRurl());

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
