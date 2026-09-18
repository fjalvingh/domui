package to.etc.domuidemo.components;

import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.dom.html.Div;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.state.UIContext;
import to.etc.domui.themes.DarkThemeVariant;
import to.etc.domui.themes.DefaultThemeVariant;
import to.etc.domui.themes.IThemeVariant;

/**
 * Switches this session between the light and the dark variant of the theme.
 *
 * <p>Both are the <i>same</i> theme: the dark one is winter with its colour file replaced,
 * which the theme search path does as soon as the variant is set. Setting it on the request
 * context stores it in the session, so the choice holds for every page after this one.</p>
 *
 * <p>The full page has to be reloaded afterwards: the stylesheet link is written by the
 * full renderer, so an ajax delta would leave the old sheet in place.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ThemeVariantSwitch extends Div {
	@Override
	public void createContent() throws Exception {
		IRequestContext ctx = UIContext.getRequestContext();
		boolean dark = DarkThemeVariant.INSTANCE.getVariantName().equals(ctx.getThemeVariant().getVariantName());

		SmallImgButton button = new SmallImgButton(dark ? Icon.faSunO : Icon.faMoonO, () -> switchTo(dark ? DefaultThemeVariant.INSTANCE : DarkThemeVariant.INSTANCE));
		add(button);
		button.setTitle(dark ? "Switch to the light theme" : "Switch to the dark theme");
	}

	private void switchTo(IThemeVariant variant) {
		UIContext.getRequestContext().setThemeVariant(variant);
		appendJavascript("WebUI.refreshPage();");
	}
}
