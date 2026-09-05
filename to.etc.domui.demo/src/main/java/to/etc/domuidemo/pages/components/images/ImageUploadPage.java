package to.etc.domuidemo.pages.components.images;

import to.etc.domui.component.image.Dimension;
import to.etc.domui.component.image.DisplayImage;
import to.etc.domui.component.image.IUIImage;
import to.etc.domui.component.image.ImageSelectControl;
import to.etc.domui.component.image.LoadedImage;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

import java.io.InputStream;

/**
 * The two image components that work on an IUIImage: the control that lets the
 * user pick one, and the read-only display of the same value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ImageUploadPage extends UrlPage {
	/** The state of this screen: the picture, as an IUIImage. */
	private IUIImage m_avatar;

	@Override
	public void createContent() throws Exception {
		setPageTitle("Images");

		if(null == m_avatar) {
			//-- Start with a picture, so there is something to see: a png beside this class.
			try(InputStream is = ImageUploadPage.class.getResourceAsStream("demo-kermit.png")) {
				m_avatar = LoadedImage.create(is, new Dimension(256, 256), null);
			}
		}

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Images"));

		//-- The read-only side: the same value, shown at three sizes.
		cp.add(new HTag(2, "DisplayImage"));
		Div displays = new Div("dm-tut");
		cp.add(displays);

		DisplayImage small = new DisplayImage(Dimension.ICON, true);
		small.setValue(m_avatar);
		displays.add(small);

		DisplayImage medium = new DisplayImage(Dimension.BIGICON, true);
		medium.setValue(m_avatar);
		displays.add(medium);

		DisplayImage large = new DisplayImage(new Dimension(96, 96), false);
		large.setValue(m_avatar);
		displays.add(large);

		DisplayImage empty = new DisplayImage(Dimension.BIGICON, true);
		empty.setDisplayEmpty(true);                      // ...and one with no value at all
		displays.add(empty);

		cp.add(new Para().add("A DisplayImage shows an IUIImage and nothing else. It does not send "
			+ "the picture with the page: it renders an img tag pointing back at itself, and serves "
			+ "the image - resized to the size asked for - from a second request. The last one has "
			+ "no value and shows the empty icon, which it only does when setDisplayEmpty() is on."));

		//-- The editable side.
		cp.add(new HTag(2, "ImageSelectControl"));
		ImageSelectControl select = new ImageSelectControl();
		select.setDisplayDimensions(new Dimension(96, 96));   // How big the thumbnail on the screen is
		select.setMaxDimensions(new Dimension(512, 512));     // What the picture is resized down to
		select.setValue(m_avatar);
		select.setEmptyIcon(Icon.faUser);
		select.setOnValueChanged(a -> {
			m_avatar = select.getValue();
			small.setValue(m_avatar);                     // The displays follow the control...
			medium.setValue(m_avatar);
			large.setValue(m_avatar);
		});

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Your avatar").control(select);

		cp.add(new Para().add("Press the picture to choose another one - any jpg, png or gif - and "
			+ "the four displays above follow it. The cross next to it clears the value. What the "
			+ "control produces is a LoadedImage: the uploaded file, resized to the maximum size "
			+ "set on the control, registered as a temporary file of the conversation so it is "
			+ "deleted when the user leaves. Storing it somewhere is the page's own job."));

		cp.add(new Para().add("Both components need ImageMagick on the server: it is what identifies "
			+ "the uploaded file and resizes it. Without it the upload fails rather than the page."));
	}
}
