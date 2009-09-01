package to.etc.domui.server.parts;

import java.io.*;

import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.image.*;

import to.etc.domui.parts.*;
import to.etc.domui.server.*;
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


	public boolean accepts(String rurl) {
		return rurl.endsWith(".png.svg");
	}
	public Object decodeKey(String rurl, IParameterInfo param) throws Exception {
		int width = PartUtil.getInt(param, "w", -1);
		int height = PartUtil.getInt(param, "h", -1);
		return new SvgKey(rurl, width, height);
	}

	public void generate(PartResponse pr, DomApplication da, Object key, ResourceDependencyList rdl) throws Exception {
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
