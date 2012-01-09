package to.etc.binaries.images;

public interface ImageInfo {
	static public final String	PNG		= "image/png";

	static public final String	JPEG	= "image/jpeg";

	static public final String	GIF		= "image/gif";

	public int getWidth();

	public int getHeight();

	public int getPage();

	public String getMime();
}
