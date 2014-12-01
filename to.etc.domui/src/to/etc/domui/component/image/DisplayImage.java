package to.etc.domui.component.image;

import to.etc.domui.component.input.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.util.*;

import javax.annotation.*;
import java.io.*;

/**
 * This displays an image that is contained in an {@link to.etc.domui.component.image.IUIImage} structure.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/1/14.
 */
public class DisplayImage extends AbstractDivControl<IUIImage> {
	public DisplayImage() {
		setCssClass("ui-dsplyima");
	}

	@Override public void createContent() throws Exception {
		Img img = new Img();
		add(img);

		if(getValueSafe() == null) {
			img.setSrc(DomUtil.getJavaResourceRURL(ImageSelectControl.class, "empty.png"));
		} else {
			String url = getComponentDataURL("THUMB", new PageParameters("datx", System.currentTimeMillis() + ""));
			img.setSrc(url);
		}
	}

	/**
	 * Called to render the image inside the component.
	 * @see to.etc.domui.dom.html.NodeBase#componentHandleWebDataRequest(to.etc.domui.server.RequestContextImpl, java.lang.String)
	 */
	@Override
	public void componentHandleWebDataRequest(RequestContextImpl ctx, String action) throws Exception {
		if("THUMB".equals(action)) {
			IUIImage image = getValueSafe();
			if(null != image)
				renderImage(ctx, image.getThumbnail());
			return;
		}

		super.componentHandleWebDataRequest(ctx, action);
	}

	private void renderImage(@Nonnull RequestContextImpl ctx, @Nonnull IUIImageInstance thumbnail) throws Exception {
		OutputStream os = ctx.getRequestResponse().getOutputStream(thumbnail.getMimeType(), null, thumbnail.getImageSize());
		InputStream is = thumbnail.getImage();
		try {
			FileTool.copyFile(os, is);
			os.close();
		} finally {
			FileTool.closeAll(os, is);
		}
	}
}

