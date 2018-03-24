package to.etc.util;

import java.io.InputStream;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-8-16.
 */
final public class Utf8EncodingChecker {
	private Utf8EncodingChecker() {}

	/**
	 * Reads the entire stream, and checks that all characters obey UTF-8 encoding format.
	 */
	static public void checkUtf8Encoding(InputStream is) throws Exception {
		//is.mark(Integer.MAX_VALUE);
		byte[] buffer = new byte[65536];
		int linenr = 1;
		int offset = 0;
		int state = 0;
		for (;;) {
			int rdlen = is.read(buffer);
			if (rdlen <= 0) {
				//is.reset();
				return;
			}

			for (int i = 0; i < rdlen; i++) {
				int v = buffer[i] & 0xff;
				if (v == '\n') {
					linenr++;
				}

				switch (state) {
					default:
						throw new IllegalStateException("Bad state: " + state);

					case 0:
						//-- reading bytes
						if (v >= 0b1111_0000) {      // 3 byte trailer
							state = 3;
						} else if (v >= 0b1110_0000) {
							state = 2;
						} else if (v >= 0b1100_0000) {
							state = 1;
						} else if (v >= 0x80) {
							// Invalid!
							throw new NoUtf8Exception(offset + i - state, linenr);
						}
						break;

					case 3:
					case 2:
					case 1:
						if ((v & 0b1100_0000) != 0b1000_0000) {
							throw new NoUtf8Exception(offset + i - state, linenr);
						}
						state--;
						break;
				}
			}
		}
	}


}
