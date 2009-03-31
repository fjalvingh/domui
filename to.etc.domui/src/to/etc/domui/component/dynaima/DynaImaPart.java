package to.etc.domui.component.dynaima;

import java.io.*;

import to.etc.domui.parts.*;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.trouble.*;
import to.etc.util.*;

public class DynaImaPart implements UnbufferedPartFactory {
	public void generate(DomApplication app, String rurl, RequestContextImpl param) throws Exception {
		DynaRenderer	cpr	= new DynaRenderer();
		cpr.generate(app, param, rurl);							// Decode input to get to the component in question.
	}

	static public class DynaRenderer extends ComponentPartRenderer {
		private DynaIma				m_ima;

		public void generate(DomApplication app, RequestContextImpl param, String rurl) throws Exception {
			initialize(app, param, rurl);
			if(getArgs().length != 3)
				throw new IllegalStateException("Invalid input URL '"+rurl+"': must be in format cid/pageclass/componentID");

			if(! (getComponent() instanceof DynaIma))
				throw new ThingyNotFoundException("The component "+getComponent().getActualID()+" on page "+getPage().getBody()+" is not an HtmlEditor instance");
			m_ima = (DynaIma) getComponent();

			//-- Check: do we already *have* a cached copy in the image? If not generate one...
			String		mime;
			int			size;
			byte[][]	data;
			synchronized(m_ima) {
				if(m_ima.getCachedData() == null) {
					m_ima.initializeCached();
				}
				mime = m_ima.getCachedMime();
				size	= m_ima.getCachedSize();
				data	= m_ima.getCachedData();
			}

			//-- Render output.
			if(data == null) {								// No data in image?
				throw new ThingyNotFoundException("No image in "+rurl);
			}

			param.getResponse().setContentType(mime);
			if(size > 0)
				param.getResponse().setContentLength(size);
			OutputStream	os	= param.getResponse().getOutputStream();
			FileTool.save(os, data);						// Flush to output
		}
	}
}
