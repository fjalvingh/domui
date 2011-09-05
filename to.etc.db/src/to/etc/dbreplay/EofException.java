package to.etc.dbreplay;

import java.io.*;

class EofException extends IOException {
	public EofException() {
		super("End of file reached");
	}
}
