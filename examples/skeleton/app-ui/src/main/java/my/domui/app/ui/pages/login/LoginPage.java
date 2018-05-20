package my.domui.app.ui.pages.login;

import my.domui.app.core.Constants;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.dom.html.Checkbox;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IReturnPressed;
import to.etc.domui.dom.html.Input;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.login.UILogin;
import to.etc.domui.state.UIGoto;
import to.etc.util.StringTool;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-7-17.
 */
public class LoginPage extends UrlPage {
	int					m_failcount;

	static public final String	FORCE_TOP	=
		"\nif(parent.location.href != self.location.href) {\n"		// Make sure we're not in a frame
//	+	"  alert('a='+parent.location.href+', b='+self.location.href);\n"
			+	"  parent.window.location.href = self.location.href;\n"
			+	"}"
		;

	@Override
	public void createContent() throws Exception {
		setTitle(Constants.APPTITLE);

		setCssClass("app-login-body");
		add(new VerticalSpacer(40));
		getPage().addHeaderContributor(HeaderContributor.loadJavaScriptlet(FORCE_TOP), 1000);

		Div container	= new Div("app-login-container");
		add(container);

		Div fd = new Div("app-login-logo");
		container.add(fd);
		fd.add(Constants.APPTITLE);

		fd = new Div("app-login-sublogo");
		container.add(fd);
		fd.add(Constants.APPMOTTO);

		fd = new Div("app-login-sublogo");
		container.add(fd);
		fd.add("Use admin@example.com with password admin");

		Div fields = new Div("app-login-form");
		container.add(fields);

		//-- error panel
		Div errorContainer = new Div("app-login-error-container");
		fields.add(errorContainer);
		errorContainer.setDisplay(DisplayType.NONE);

		//-- Email field
		Div field = new Div("app-login-field");
		fields.add(field);
		//Text2<String> emailIn = new Text2<>(String.class);
		Input emailIn = new Input();
		emailIn.setPlaceHolder("email");
		Label emailL = new Label();
		emailL.add(new FaIcon(FaIcon.faUser));
		emailL.setForTarget(emailIn);
		field.add(emailL);
		field.add(emailIn);

		//-- Password field
		field = new Div("app-login-field");
		fields.add(field);
		Input pwIn = new Input();
		pwIn.setInputType("password");
		pwIn.setPlaceHolder("password");
		Label pwL = new Label();
		pwL.add(new FaIcon(FaIcon.faLock));
		pwL.setForTarget(pwIn);
		field.add(pwL);
		field.add(pwIn);

		Checkbox keeplogin = new Checkbox();

		//-- Login button
		field = new Div("app-login-field");
		fields.add(field);
		DefaultButton login = new DefaultButton("Log in", a -> doLogin(emailIn, pwIn, keeplogin, errorContainer)).css("is-primary");
		field.add(login);

		//-- Remember me
		field = new Div("app-login-field");
		fields.add(field);
		Div both = new Div("app-login-check");
		field.add(both);

		Span span = new Span();
		both.add(span);
		span.add("Remember me");
		span.setCssClass("app-login-cblabel");
		span.add(keeplogin);

		container.setReturnPressed((IReturnPressed<Div>) node -> doLogin(emailIn, pwIn, keeplogin, errorContainer));
	}

	private void error(NodeContainer errorContainer, String message) {
		Div error = new Div("app-login-error");
		errorContainer.add(error);
		error.add(message);
		errorContainer.setDisplay(DisplayType.BLOCK);
	}

	private void doLogin(Input emailIn, Input pwIn, Checkbox keeplogin, NodeContainer errorContainer) {
		try {
			errorContainer.setDisplay(DisplayType.NONE);
			errorContainer.removeAllChildren();

			String email = emailIn.getRawValue();
			if(email == null || StringTool.isBlank(email)) {
				emailIn.addCssClass("ui-input-err");
				error(errorContainer, "email address missing");
				return;
			}
			emailIn.removeCssClass("ui-input-err");

			String pw = pwIn.getRawValue();
			if(pw == null || StringTool.isBlank(pw)) {
				pwIn.addCssClass("ui-input-err");
				error(errorContainer, "password is missing");
				return;
			}
			pwIn.removeCssClass("ui-input-err");

			/*
			 * When there are too many failures: just fake the login but do not do it anymore.
			 */
			if(m_failcount > 10 || !UILogin.login(email, pw)) {
				error(errorContainer, "Invalid login");
				m_failcount++;
				if(m_failcount > 3) {
					Thread.sleep(4000);
				}
			} else {
				if(keeplogin.isChecked()) {
					//-- Create a LOGIN cookie
					UILogin.createLoginCookie(System.currentTimeMillis() + 30l * 1000 * 24 * 60 * 60);
				}

				/*
				 * We put the page to return-to in the session to prevent putting shit in the root url; it will
				 * be cleared from the session in the menu.
				 */
				String tgt = getPage().getPageParameters().getString("target");
				if(null == tgt) {
					tgt = "";
				}
				UIGoto.redirect(tgt);
				return;
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}
