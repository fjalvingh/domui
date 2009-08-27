package to.etc.binaries.images;

import java.io.*;
import java.util.*;

public interface ImageHandler {
	public List<ImagePage> identify(File input) throws Exception;

	public ImageDataSource thumbnail(File inf, int page, int w, int h, String mime) throws Exception;

	public ImageDataSource scale(File inf, int page, int w, int h, String mime) throws Exception;
}
