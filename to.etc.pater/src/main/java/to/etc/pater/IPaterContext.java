package to.etc.pater;

import java.io.*;

public interface IPaterContext {
	public void registerResult(String description, String mimeType, File resource) throws Exception;
}
