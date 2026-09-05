package to.etc.domuidemo.pages.components.images;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * Img: the plain html image tag, and the three places its source can come from.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ImgPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Img");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Img"));

		//-- Where the source can come from: the web application, the theme, a java resource.
		cp.add(new HTag(2, "Where the picture comes from"));
		Div sources = new Div("dm-tut");
		cp.add(sources);

		Img fromWebapp = new Img("img/logo-small.png");   // A path in the web application
		fromWebapp.setAlt("The DomUI logo");
		sources.add(labelled("img/logo-small.png", fromWebapp));

		//-- ...from the current theme's directory
		sources.add(labelled("THEME/btnSave.png", new Img("THEME/btnSave.png")));

		//-- ...and next to this class, as a java resource
		sources.add(labelled("beside this class", new Img(ImgPage.class, "demo-kermit.png")));

		cp.add(new Para().add("Three sources, one tag: a path inside the web application, a path "
			+ "starting with THEME/ which resolves against the theme the application is running, "
			+ "and a java resource beside a class - which is how a component ships its own images "
			+ "in its own jar."));

		//-- Size, on the tag itself.
		cp.add(new HTag(2, "Size"));
		Div sizes = new Div("dm-tut");
		cp.add(sizes);
		for(int width : new int[]{32, 64, 128}) {
			Img img = new Img(ImgPage.class, "demo-kermit.png");
			img.setImgWidth(Integer.toString(width));
			img.setAlt("Kermit at " + width + " pixels");
			sizes.add(img);
		}

		cp.add(new Para().add("setImgWidth() and setImgHeight() write the tag's own width and "
			+ "height attributes, so the browser scales the picture. They take strings because the "
			+ "html attributes do; giving only one of the two keeps the aspect ratio."));

		//-- An image that answers a click, and one that refuses to.
		cp.add(new HTag(2, "An image you can press"));
		Div clickable = new Div("dm-tut");
		cp.add(clickable);

		Img button = new Img("img/reload.png");
		button.setTitle("Press me");
		button.setClicked(a -> MsgBox2.on(this).info("The image was clicked"));
		clickable.add(button);

		Img disabled = new Img("img/reload.png");
		disabled.setTitle("This one is disabled");
		disabled.setClicked(a -> MsgBox2.on(this).info("...which you should never see"));
		disabled.setDisabled(true);
		clickable.add(disabled);

		cp.add(new Para().add("Giving an Img a click handler makes it clickable - the cursor "
			+ "changes, because the tag gets the ui-clickable class. The second one is disabled: the "
			+ "handler is not called, and the picture is grey because the src now points at the "
			+ "framework's grayscaler, which makes a grey copy of it on the server."));

		cp.add(new Para().add("For something that is really a button, though, use a button: "
			+ "SmallImgButton and HoverButton are images that know they are buttons."));
	}

	/**
	 * One image with a caption under it, so the examples can be told apart.
	 */
	private static Div labelled(String text, Img img) {
		Div d = new Div();
		d.setDisplay(DisplayType.INLINE_BLOCK);
		d.setMargin("8px");
		d.add(new Div().add(img));
		d.add(new Div().add(text));
		return d;
	}
}
