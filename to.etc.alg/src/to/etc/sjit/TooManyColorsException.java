package to.etc.sjit;

import java.io.*;

/**
 * Thrown when a GIF image is encoded and the resulting image has too many
 * colors. To prevent this one can use the color quantizer on the source image
 * before adding it again.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TooManyColorsException extends IOException {
	public TooManyColorsException() {
		super("More than 255 colors in this GIF are not allowed.");
	}
}
