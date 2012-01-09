package to.etc.server.misc;

import javax.servlet.http.*;

/**
 * A servlet helper interface abstracting something that can output shit to
 * a response directly.
 *
 * @author jal
 * Created on Feb 17, 2005
 */
public interface HttpResponseGenerator {
	public void generate(HttpServletResponse res) throws Exception;

	public long getLastModified() throws Exception;
}
