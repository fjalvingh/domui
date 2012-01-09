package to.etc.log;

import java.util.*;

/**
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public interface iLogWriter {
	public void initialize(LogMaster l, Properties p) throws Exception;

	public void write(Object l, Category c1, Category c2, Category c3, Category c4, String msg, Exception x) throws Exception;
}
