package to.etc.domui.component.upload;

import to.etc.domui.parts.*;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.util.*;
import to.etc.domui.util.upload.*;

/**
 * This thingy accepts file upload requests for a given control.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 14, 2008
 */
public class UploadPart implements IUnbufferedPartFactory {
	@Override
	public void generate(DomApplication app, String rurl, RequestContextImpl param) throws Exception {
		try {
			ComponentPartRenderer r = new ComponentPartRenderer();
			r.initialize(app, param, rurl);
			FileUpload fu = (FileUpload) r.getComponent();
			UploadItem[] uiar = param.getFileParameter(fu.getInput().getActualID());
			if(uiar != null) {
				for(UploadItem ui : uiar) {
					fu.getFiles().add(ui);
					r.getConversation().registerUploadTempFile(ui.getFile());
				}
			}
			fu.forceRebuild();

			//-- Render an optimal delta as the response,
			ServerTools.generateNoCache(param.getResponse()); // Do not allow the browser to cache
			ApplicationRequestHandler.renderOptimalDelta(param, r.getPage());
		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}
