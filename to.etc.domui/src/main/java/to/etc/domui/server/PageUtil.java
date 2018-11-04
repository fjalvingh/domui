package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.ProgrammerErrorException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 4-11-18.
 */
final class PageUtil {
	private PageUtil() {
	}

	/**
	 * Decide what class to run depending on the input path.
	 */
	@NonNull
	static Class< ? extends UrlPage> decodeRunClass(@NonNull final IRequestContext ctx) {
		DomApplication application = ctx.getApplication();
		if(ctx.getInputPath().length() == 0) {
			/*
			 * We need to EXECUTE the application's main class. We cannot use the .class directly
			 * because the reloader must be able to substitute a new version of the class when
			 * needed.
			 */
			Class< ? extends UrlPage> rootPage = application.getRootPage();
			if(null == rootPage)
				throw new ProgrammerErrorException("The DomApplication's 'getRootPage()' method returns null, and there is a request for the root of the web app... Override that method or make sure the root is handled differently.");
			String txt = rootPage.getCanonicalName();
			return application.loadPageClass(txt);
		}

		//-- Try to resolve as a class name,
		String s = ctx.getInputPath();
		int pos = s.lastIndexOf('.');							// Always strip whatever extension
		if(pos != -1) {
			int spos = s.lastIndexOf('/') + 1;					// Try to locate path component
			if(pos > spos) {
				s = s.substring(spos, pos); 						// Last component, ex / and last extension.

				//-- This should be a classname now
				return application.loadPageClass(s);
			}
		}

		//-- All others- cannot resolve
		throw new IllegalStateException("Cannot decode URL " + ctx.getInputPath());
	}



}

