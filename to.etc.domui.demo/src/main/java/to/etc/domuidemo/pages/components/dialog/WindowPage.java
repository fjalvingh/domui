package to.etc.domuidemo.pages.components.dialog;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.Window;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * Window: a floating window with a title bar, a scrolling content area and two
 * fixed areas around it. It has no buttons and no logic - that is what Dialog
 * adds.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class WindowPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Window");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Window"));

		Div result = new Div("dm-tut-q");
		result.add("Close a window and the reason it closed with appears here.");

		Div buttons = new Div("dm-tut");
		cp.add(buttons);

		//-- The simplest window: modal, not resizable, of the default size.
		buttons.add(new DefaultButton("A modal window", a -> {
			Window w = new Window("The album");
			add(w);                                       // An overlay is added to the page, not to the content panel
			w.add(new Para().add("Everything added to the window ends up in its content area, "
				+ "which is the part that scrolls."));
		}));

		//-- An icon in the title bar: any icon reference, so a font icon too.
		buttons.add(new DefaultButton("With an icon in the title bar", a -> {
			Window w = new Window("Tracks");
			w.setIcon(Icon.faMusic);
			add(w);
			w.add(new Para().add("The icon in the title bar is an IIconRef, so it is a font "
				+ "icon here and an image when the reference points at one."));
		}));

		//-- Not modal, resizable, and of a given size.
		buttons.add(new DefaultButton("Movable and resizable", a -> {
			Window w = new Window(false, true, 500, 300, "Drag my title bar");
			add(w);
			w.add(new Para().add("This window is not modal, so the page behind it still answers. "
				+ "Drag the title bar to move it, and the bottom right corner to resize it."));
		}));

		//-- The two areas that do not scroll with the content.
		buttons.add(new DefaultButton("Fixed areas around the content", a -> {
			Window w = new Window(true, false, 500, 400, "A window with fixed areas");
			add(w);

			w.getTopContent().setHeight("24px");          // The height must be set, or the layout cannot be computed
			w.getTopContent().add("This line stays where it is.");

			w.getBottomContent().setHeight("40px");
			w.getBottomContent().add(new DefaultButton("Close", b -> w.close()));

			for(int i = 1; i <= 40; i++) {
				w.add(new Div().add("Content line " + i + ", which scrolls between the two."));
			}
		}));

		//-- Told when the user closes it, and by which route.
		buttons.add(new DefaultButton("Told when it closes", a -> {
			Window w = new Window(true, false, 450, -1, "Close me");
			add(w);
			w.setOnClose(reason -> {
				result.removeAllChildren();
				result.add("The window closed, reason: " + reason);
			});
			w.add(new Para().add("Press the cross: that is a user action, so the close handler runs."));
			w.add(new DefaultButton("Close from code", b -> w.close()));
			w.add(new DefaultButton("Cancel, as the cross does", b -> w.closePressed()));
		}));

		//-- No cross in the title bar: the window itself decides when it goes.
		buttons.add(new DefaultButton("Without a close button", a -> {
			Window w = new Window(true, false, 400, -1, "No way out but one");
			w.setClosable(false);                         // No cross in the title bar
			w.setAutoClose(false);                        // ...and clicking outside it does not close it either
			add(w);
			w.add(new Para().add("This window has no cross, and clicking next to it does nothing."));
			w.add(new DefaultButton("Let me out", b -> w.close()));
		}));

		cp.add(result);

		cp.add(new Para().add("A Window is presentation only: a title bar you can drag, a content area "
			+ "that scrolls, and a fixed area above and below it. close() removes it silently; "
			+ "closePressed() - which is what the cross does - removes it and calls the close handler."));
	}
}
