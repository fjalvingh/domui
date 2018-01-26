package to.etc.dbreplay;

import java.io.IOException;

class EofException extends IOException {
	public EofException() {
		super("End of file reached");
	}
}
