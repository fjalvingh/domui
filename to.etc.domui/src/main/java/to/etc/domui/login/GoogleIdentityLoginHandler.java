package to.etc.domui.login;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.dom.header.GoogleIdentificationContributor;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.IRequestContext;
import to.etc.function.ConsumerEx;

import java.util.Collections;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-3-18.
 */
final public class GoogleIdentityLoginHandler {
	static private final Logger LOG = LoggerFactory.getLogger(GoogleIdentityLoginHandler.class);

	private final UrlPage m_page;

	private final String m_clientID;

	private final ConsumerEx<Profile> m_onAuthentication;

	public static final class Profile {
		private final String m_email;

		private final String m_name;

		private final String m_pictureUrl;

		private final String m_locale;

		private final String m_familyName;

		private final String m_givenName;

		private final boolean m_emailVerified;

		public Profile(String email, String name, String pictureUrl, String locale, String familyName, String givenName, boolean emailVerified) {
			m_email = email;
			m_name = name;
			m_pictureUrl = pictureUrl;
			m_locale = locale;
			m_familyName = familyName;
			m_givenName = givenName;
			m_emailVerified = emailVerified;
		}

		public String getEmail() {
			return m_email;
		}

		public String getName() {
			return m_name;
		}

		public String getPictureUrl() {
			return m_pictureUrl;
		}

		public String getLocale() {
			return m_locale;
		}

		public String getFamilyName() {
			return m_familyName;
		}

		public String getGivenName() {
			return m_givenName;
		}

		public boolean isEmailVerified() {
			return m_emailVerified;
		}
	}

	private GoogleIdentityLoginHandler(UrlPage page, String id, ConsumerEx<Profile> onAuthentication) {
		m_page = page;
		m_clientID = id;
		m_onAuthentication = onAuthentication;
	}

	/**
	 * Connect the authenticator to a login page.
	 */
	static public GoogleIdentityLoginHandler create(UrlPage page, String clientID, ConsumerEx<Profile> onAuthentication) {
		GoogleIdentityLoginHandler handler = new GoogleIdentityLoginHandler(page, clientID, onAuthentication);
		page.getPage().addHeaderContributor(new GoogleIdentificationContributor(clientID), 100);
		page.addListener((actionName, context) -> {
			if(! "GOOGLELOGIN".equals(actionName))
				return false;
			handler.onWebAction(context);
			return true;
		});

		return handler;
	}

	public Div getLoginButton() {
		Div gg = new Div("g-signin2");
		gg.setSpecialAttribute("data-onsuccess", "googleOnSignin");
		return gg;
	}

	public boolean onWebAction(IRequestContext context) {
		String token = context.getParameter("token");

		//-- Validate the token.
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
				.setAudience(Collections.singletonList(m_clientID))
				.build();

		try {
			GoogleIdToken idToken = verifier.verify(token);
			if(idToken == null) {
				LOG.info("Missing  Google authentication token");
				return true;
			}
			GoogleIdToken.Payload payload = idToken.getPayload();
			String userId = payload.getSubject();
			System.out.println("User ID: " + userId);

			// Get profile information from payload
			String email = payload.getEmail();
			boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
			String name = (String) payload.get("name");
			String pictureUrl = (String) payload.get("picture");
			String locale = (String) payload.get("locale");
			String familyName = (String) payload.get("family_name");
			String givenName = (String) payload.get("given_name");

			Profile profile = new Profile(email, name, pictureUrl, locale, familyName, givenName, emailVerified);
			m_onAuthentication.accept(profile);

//			System.out.println("email=" + email + " name=" + name + " image=" + pictureUrl + " token=" + token);

		} catch(Exception x) {
			LOG.error("Authentication exception: " + x, x);
		}
		return true;
	}

	public static void logout(NodeContainer node, String key) {

	}

}
