package to.etc.server.servlet.cmd;

import java.io.*;

public class URLCommandContext extends CommandContext {
	public URLCommandContext(CommandServletContext ctx, String name) throws IOException {
		super(ctx, name);
	}

	@Override
	public String getStringParam(String name, String def) throws Exception {
		String s = getContext().getRequest().getParameter(name);
		return s == null ? def : s;
	}
}
