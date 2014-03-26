package to.etc.domui.server;

import javax.annotation.*;

import to.etc.util.*;

public class ExceptionUtil {
	@Nonnull
	final private RequestContextImpl m_ctx;

	public ExceptionUtil(@Nonnull RequestContextImpl ri) {
		m_ctx = ri;
	}

	public String renderParameters() {
		StringBuilder sb = new StringBuilder();
		sb.append("<table class='listtbl'>\n");
		sb.append("<thead><tr>\n");
		sb.append("<th>name</th><th>Value</th>");
		sb.append("</tr></thead>\n");
		String[] names = m_ctx.getParameterNames();
		if(names != null) {
			for(String name: names) {
				boolean first = true;
				String[] values = m_ctx.getParameters(name);
				if(values == null || values.length == 0) {
					sb.append("<tr><td>").append(StringTool.htmlStringize(name)).append("</td><td>No value</td></tr>");
				} else {
					for(String value : values) {
						sb.append("<tr><td>");
						if(first)
							sb.append(StringTool.htmlStringize(name));
						else
							sb.append("\u00a0");
						first = false;
						sb.append("</td><td>");

						sb.append(StringTool.htmlStringize(value));
						sb.append("</td></tr>");
					}
				}
			}
		}
		sb.append("</table>");
		return sb.toString();
	}

}
