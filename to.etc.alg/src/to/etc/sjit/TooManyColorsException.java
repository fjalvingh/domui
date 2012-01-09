package to.etc.sjit;

import java.io.*;

/**
 * Thrown when a GIF image is encoded and the resulting image has too many
 * colors. To prevent this one can use the color quantizer on the source image
 * before adding it again.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class TooManyColorsException extends IOException {
	public TooManyColorsException() {
		super("More than 255 colors in this GIF are not allowed.");
	}
}
