package to.etc.domui.util;

import java.io.*;

import to.etc.domui.util.resources.*;

public interface IResourceRef extends IWithModifiedCalculator {
	public InputStream	getInputStream() throws Exception;
}
