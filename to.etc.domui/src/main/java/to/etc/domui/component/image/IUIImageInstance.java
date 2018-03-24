package to.etc.domui.component.image;

import java.io.*;

import javax.annotation.*;

import to.etc.domui.server.parts.*;

/**
 * A specialization (like a resized version) of a known {@link IUIImage}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 5, 2014
 */
public interface IUIImageInstance {
	/**
	 * Return an input stream that contains the byte data for the current image. If the
	 * image has been set with uploaded() then this returns the input stream for that
	 * uploaded file. If the image comes from "somewhere else" this returns a stream
	 * for "somewhere else". This method will get called outside the context of the form
	 * in a separate request (from an {@link IUnbufferedPartFactory}).
	 * @return
	 */
	@Nonnull InputStream getImage() throws Exception;

	/**
	 * The size of the image, in bytes, if known; -1 if unknown.
	 * @return
	 */
	int getImageSize();

	/**
	 * The size of the image, in pixels.
	 * @return
	 */
	@Nonnull Dimension getDimension() throws Exception;

	/**
	 * Return the mime type of the current image, which must be one of the core image mime
	 * types for png, jpg or gif.
	 * @return
	 */
	@Nonnull String getMimeType() throws Exception;

}
