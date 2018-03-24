package to.etc.domui.component.image;

import to.etc.domui.component.input.AbstractDivControl;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.state.PageParameters;
import to.etc.domui.themes.Theme;
import to.etc.util.FileTool;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This displays an image that is contained in an {@link to.etc.domui.component.image.IUIImage} structure.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12/1/14.
 */
public class DisplayImage extends AbstractDivControl<IUIImage> {
	private boolean	m_displayEmpty;

	private boolean m_thumbnail;

	private Dimension m_maxSize;

	public DisplayImage(@Nonnull Dimension size, boolean thumb) {
		setCssClass("ui-dsplyima");
		m_thumbnail = thumb;
		m_maxSize = size;
	}

	public DisplayImage() {
		this(Dimension.ICON, true);
	}

	@Override public void createContent() throws Exception {
		if(getValueSafe() == null) {
			if(m_displayEmpty) {
				Img img = new Img();
				add(img);
				img.setSrc(Theme.ISCT_EMPTY);
			}
		} else {
			Img img = new Img();
			add(img);

			String url = getComponentDataURL("THUMB", new PageParameters("datx", System.currentTimeMillis() + ""));
			img.setSrc(url);
		}
	}

	@Nullable @Override public NodeBase getForTarget() {
		return null;
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
				renderImage(ctx, image);
			return;
		}

		super.componentHandleWebDataRequest(ctx, action);
	}

	private void renderImage(@Nonnull RequestContextImpl ctx, @Nonnull IUIImage thumbnail) throws Exception {
		IUIImageInstance ii = thumbnail.getImage(m_maxSize, m_thumbnail);
		OutputStream os = ctx.getRequestResponse().getOutputStream(ii.getMimeType(), null, ii.getImageSize());
		InputStream is = ii.getImage();
		try {
			FileTool.copyFile(os, is);
			os.close();
		} finally {
			FileTool.closeAll(os, is);
		}
	}

	public boolean isDisplayEmpty() {
		return m_displayEmpty;
	}

	public void setDisplayEmpty(boolean displayEmpty) {
		if(m_displayEmpty == displayEmpty)
			return;
		m_displayEmpty = displayEmpty;
		forceRebuild();
	}

	public void setSize(@Nullable Dimension dimension) {
		m_maxSize = dimension;
		forceRebuild();
	}

	public void setThumbnail(@Nullable Dimension size) {
		m_maxSize = size == null ? Dimension.ICON : size;
		m_thumbnail = true;
		forceRebuild();
	}
}
