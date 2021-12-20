package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.themes.ThemeManager;
import to.etc.template.JSTemplate;
import to.etc.template.JSTemplateCompiler;
import to.etc.util.FileTool;
import to.etc.util.StringTool;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-11-18.
 */
@NonNullByDefault
public class OopsFrameRenderer {
	@Nullable
	private JSTemplate m_exceptionTemplate;

	protected String m_templateName = "exceptionTemplate.html";

	protected String m_stacktraceTag = "a";

	public void renderOopsFrame(RequestContextImpl ctx, Throwable x, boolean testMode) throws Exception {
		if(!testMode) {
			m_templateName = "exceptionPrdTemplate.html";
			m_stacktraceTag = "label";
		}
		if(ctx.getRequestResponse() instanceof HttpServerRequestResponse) {
			HttpServerRequestResponse srr = (HttpServerRequestResponse) ctx.getRequestResponse();
			HttpServletResponse resp = srr.getResponse();
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);                // Fail with proper response code.
		}
		DomApplication.get().getDefaultHTTPHeaderMap().forEach((header, value) -> ctx.getRequestResponse().addHeader(header, value));

		ThemeManager themeManager = ctx.getApplication().internalGetThemeManager();

		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("x", x);
		dataMap.put("ctx", ctx);
		dataMap.put("app", ctx.getRelativePath(""));
		String sheet = themeManager.getThemedResourceRURL(ctx, "THEME/style.theme.css");
		if(null == sheet)
			throw new IllegalStateException("Unexpected null??");
		dataMap.put("stylesheet", sheet);

		String theme = themeManager.getThemedResourceRURL(ctx, "THEME/");
		dataMap.put("theme", theme);

		StringBuilder sb = new StringBuilder();
		dumpException(sb, x);
		dataMap.put("stacktrace", sb.toString());
		dataMap.put("message", StringTool.htmlStringize(x.toString()));
		dataMap.put("ctx", ctx);
		ExceptionUtil util = new ExceptionUtil(ctx);
		dataMap.put("util", util);

		//util.renderEmail(x);

		Writer w = ctx.getRequestResponse().getOutputWriter("text/html", "utf-8");
		JSTemplate xt = getExceptionTemplate();
		xt.execute(w, dataMap);
		w.flush();
		w.close();
	}

	private void dumpException(@NonNull StringBuilder a, @NonNull Throwable x) {
		Set<String> allset = new HashSet<>();
		StackTraceElement[] ssear = x.getStackTrace();
		for(StackTraceElement sse : ssear) {
			allset.add(sse.toString());
		}

		dumpSingle(a, x, Collections.EMPTY_SET);

		Throwable curr = x;
		for(; ; ) {
			Throwable cause = curr.getCause();
			if(cause == null || cause == curr)
				break;

			a.append("\n\n     Caused by ").append(StringTool.htmlStringize(cause.toString())).append("\n");
			dumpSingle(a, cause, allset);
			curr = cause;
		}
	}

	private void dumpSingle(@NonNull StringBuilder sb, @NonNull Throwable x, @NonNull Set<String> initset) {
		//-- Try to render openable stack trace elements as links.
		List<StackTraceElement> list = Arrays.asList(x.getStackTrace());

		//-- Remove from the end the server stuff
		int ix = findName(list, AppFilter.class.getName());
		if(ix != -1) {
			list = new ArrayList<>(stripFrames(list, ix + 1));
		}

		//-- Remove from the end all names in initset.
		for(int i = list.size(); --i >= 0; ) {
			String str = list.get(i).toString();
			if(!initset.contains(str))
				break;
			list.remove(i);
		}

		for(StackTraceElement ste : list) {
			appendTraceLink(sb, ste);
		}
		if(x instanceof SQLException) {
			SQLException sx = (SQLException) x;
			while(sx.getNextException() != null) {
				sx = sx.getNextException();
				sb.append("SQL NextException: ");
				sb.append(StringTool.htmlStringize(sx.toString()));
				sb.append("<br>");
			}
		}
	}

	private static int findName(@NonNull List<StackTraceElement> list, String name) {
		for(int i = list.size(); --i >= 0; ) {
			String cn = list.get(i).getClassName();
			if(name.equals(cn))
				return i;
		}
		return -1;
	}

	private List<StackTraceElement> stripFrames(@NonNull List<StackTraceElement> list, int from) {
		return list.subList(0, from - 1);
	}

	private void appendTraceLink(@NonNull StringBuilder sb, @NonNull StackTraceElement ste) {
		if(m_stacktraceTag == "a") {
			sb.append("        <" + m_stacktraceTag + " class='exc-stk-l' href=\"#\" onclick=\"linkClicked('");
		} else {
			sb.append("        <" + m_stacktraceTag + " class='exc-stk-l'");
		}
		//-- Get name for the thingy,
		String name;
		if(ste.getLineNumber() <= 0)
			name = ste.getClassName().replace('.', '/') + ".java@" + ste.getMethodName();
		else
			name = ste.getClassName().replace('.', '/') + ".java#" + ste.getLineNumber();
		sb.append(StringTool.htmlStringize(name));
		if(m_stacktraceTag == "a") {
			sb.append("')\">");
		} else {
			sb.append("\">");
		}
		sb.append(ste).append("</" + m_stacktraceTag + "><br>");
	}

	@NonNull
	public JSTemplate getExceptionTemplate() throws Exception {
		JSTemplate xt = m_exceptionTemplate;
		if(xt == null) {
			JSTemplateCompiler jtc = new JSTemplateCompiler();
			File src = new File(getClass().getResource(m_templateName).getFile());
			if(src.exists() && src.isFile()) {
				Reader r = new FileReader(src);
				try {
					xt = jtc.compile(r, src.getAbsolutePath());
				} finally {
					FileTool.closeAll(r);
				}
			} else {
				xt = jtc.compile(ApplicationRequestHandler.class, m_templateName, "utf-8");
			}
			m_exceptionTemplate = xt;
		}
		return xt;
	}
}
