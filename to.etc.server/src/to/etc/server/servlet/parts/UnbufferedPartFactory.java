package to.etc.server.servlet.parts;

import to.etc.server.servlet.*;
import to.etc.server.vfs.*;

public interface UnbufferedPartFactory extends PartFactory {
	public void generate(RequestContext rctx, String rurl, VfsPathResolver r) throws Exception;
}
