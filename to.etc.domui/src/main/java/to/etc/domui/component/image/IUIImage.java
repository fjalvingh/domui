package to.etc.domui.component.image;

import javax.annotation.*;

/**
 * Base interface to allow image load and display inside control(s).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 5, 2014
 */
public interface IUIImage {
	/**
	 * Return an image that fits the specified size. If size is null then the image is returned verbatim, without any resize
	 * unless thumbnail == true; in that case size is assumed to be 16x16.
	 * @param size
	 * @return
	 * @throws Exception
	 */
	@Nonnull IUIImageInstance getImage(@Nullable Dimension size, boolean thumbNail) throws Exception;

	@Nullable Long getId();

	void setId(Long id);
}
