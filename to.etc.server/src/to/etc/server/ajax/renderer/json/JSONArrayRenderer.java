package to.etc.server.ajax.renderer.json;

import java.lang.reflect.*;

import to.etc.util.*;

abstract public class JSONArrayRenderer extends JSONItemRenderer {
	private int	m_perLine;

	public JSONArrayRenderer(int pl) {
		m_perLine = pl;
	}

	@Override
	final public void render(JSONRenderer r, Object val) throws Exception {
		IndentWriter w = r.getWriter();
		w.print("[");
		w.inc();
		int len = Array.getLength(val);
		int lc = 0;
		for(int i = 0; i < len; i++) {
			if(i != 0)
				w.print(",");
			render(r, val, i);
			if(lc > m_perLine) {
				lc = 0;
				w.println();
			}
		}
		w.dec();
		w.print("]");
	}

	abstract public void render(JSONRenderer r, Object val, int ix) throws Exception;
}
