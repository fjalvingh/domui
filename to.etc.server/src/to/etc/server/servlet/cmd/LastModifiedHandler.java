package to.etc.server.servlet.cmd;

import javax.servlet.http.*;

public interface LastModifiedHandler {
	public void setResponse(HttpServletResponse res, boolean ispost);

	public long getLastModified() throws Exception;
}
