package to.etc.domui.parts;

import java.io.*;

import javax.annotation.*;
import javax.servlet.http.*;

import to.etc.domui.component.misc.*;
import to.etc.domui.component.misc.InternalParentTree.AnswerType;
import to.etc.domui.component.misc.InternalParentTree.CommandResponse;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;

public class OpenStacktracePart implements IUnbufferedPartFactory {
	@Override
	public void generate(@Nonnull DomApplication app, @Nonnull String rurl, @Nonnull RequestContextImpl param) throws Exception {
		//-- Get the stacktrace element,
		if(!(param.getRequestResponse() instanceof HttpServerRequestResponse))
			return;
		HttpServletResponse r = ((HttpServerRequestResponse) param.getRequestResponse()).getResponse();
		r.setStatus(200);
		r.setContentType("application/json");
		r.setCharacterEncoding("utf-8");
		PrintWriter w = r.getWriter();

		String check = param.getParameter("check");
		if(null != check) {
			w.print("{\"reload\":");
			w.print(Long.toString(ReloadingContextMaker.getLastReload()));
			w.print(".0}");
		} else {
			String stk = param.getParameter("element");
			if(stk == null || stk.trim().length() == 0)
				throw new IllegalArgumentException("Missing 'element' argument");

			CommandResponse rc = InternalParentTree.openEclipseSource(stk);
			w.print("{\"message\":\"");
			if(rc.getType() != AnswerType.SUCCESS) {
				w.print(InternalParentTree.getResponseMessage(rc));
			}
			w.print("\"}");
		}
	}


}
