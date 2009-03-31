package to.etc.domui.util.images.converters;

import java.io.*;

public interface IImageIdentifier {
	public ImageData		identifyImage(File src, String mime);
}
