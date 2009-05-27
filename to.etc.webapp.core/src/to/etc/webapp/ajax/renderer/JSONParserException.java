package to.etc.webapp.ajax.renderer;

import java.io.*;

public class JSONParserException extends IOException {
	public JSONParserException() {}

	public JSONParserException(final String message) {
		super(message);
	}
}
