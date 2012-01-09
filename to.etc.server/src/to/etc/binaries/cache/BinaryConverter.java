package to.etc.binaries.cache;

import to.etc.binaries.images.*;

/**
 * A factory able to convert binaries from one format to another format.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 10, 2006
 */
public interface BinaryConverter {
	public boolean accepts(BinaryInfo corebi, String type, String mime, int width, int height);

	public ImageDataSource generate(BinaryRef source, String type, String mime, int w, int h) throws Exception;
}
