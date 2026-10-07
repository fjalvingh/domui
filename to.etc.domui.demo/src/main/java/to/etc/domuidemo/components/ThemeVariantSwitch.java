package to.etc.domuidemo.components;

import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.dom.html.Div;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.state.UIContext;
import to.etc.domui.themes.IThemeVariant;
import to.etc.domui.themes.ThemeNature;

/**
 * Switches this session between light and dark: to the application's first light colour
 * scheme from any dark one, and to its first dark scheme from any light one.
 *
 * <p>Every scheme is the <i>same</i> theme with other colours, which the theme search path
 * gives as soon as the variant is set. Setting it on the request context stores it in the
 * session, so the choice holds for every page after this one.</p>
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
		boolean dark = ThemeNature.DARK.getName().equals(ctx.getThemeVariant().getColorScheme());
		IThemeVariant other = DomApplication.get().getThemeVariantForColorScheme(dark ? ThemeNature.LIGHT.getName() : ThemeNature.DARK.getName());
		if(null == other)
			return;

		SmallImgButton button = new SmallImgButton(dark ? Icon.faSunO : Icon.faMoonO, () -> switchTo(other));
		add(button);
		button.setTitle(dark ? "Switch to the light theme" : "Switch to the dark theme");
	}

	private void switchTo(IThemeVariant variant) {
		UIContext.getRequestContext().setThemeVariant(variant);
		appendJavascript("WebUI.refreshPage();");
	}
}
