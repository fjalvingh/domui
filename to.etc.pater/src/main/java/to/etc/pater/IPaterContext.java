package to.etc.pater;

import java.io.*;

public interface IPaterContext {
	void registerResult(String description, String mimeType, File resource) throws Exception;
}
