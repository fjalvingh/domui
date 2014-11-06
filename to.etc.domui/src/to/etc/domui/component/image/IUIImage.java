package to.etc.domui.component.image;

import javax.annotation.*;

/**
 * Base interface to allow image load and display inside control(s).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 5, 2014
 */
public interface IUIImage {
	@Nonnull
	public IUIImageInstance getImage() throws Exception;

	@Nonnull
	public IUIImageInstance getThumbnail() throws Exception;
}
