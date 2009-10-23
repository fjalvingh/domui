package to.etc.domui.state;

import java.lang.reflect.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

/**
 * This static class helps with constructing pages from NodeContainer classes
 * that are marked as being usable as pages.
 * Parking class which holds the code to create a page class, including all
 * embellishments.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 23, 2008
 */
public class PageMaker {
	/**
	 * This tries to locate the specified page class in the conversation specified, and returns
	 * null if the page cannot be located. It is a helper function to allow access to components
	 * from Parts etc.
	 */
	static public Page findPageInConversation(final IRequestContext rctx, final Class< ? extends UrlPage> clz, final String cid) throws Exception {
		if(cid == null)
			return null;
		String[] cida = DomUtil.decodeCID(cid);
		WindowSession cm = rctx.getSession().findWindowSession(cida[0]);
		if(cm == null)
			throw new IllegalStateException("The WindowSession with wid=" + cida[0] + " has expired.");
		ConversationContext cc = cm.findConversation(cida[1]);
		if(cc == null)
			return null;

		//-- Page resides here?
		return cc.findPage(clz); // Is this page already current in this context?
	}

	/**
	 * FIXME Move to WindowSession?
	 * @param pg
	 * @param papa
	 * @return
	 * @throws Exception
	 */
	static public boolean pageAcceptsParameters(final Page pg, final PageParameters papa) throws Exception {
		if(papa == null)
			return true;
		if(papa.equals(pg.getPageParameters()))
			return true;
		UrlPage nc = pg.getBody();
		if(nc instanceof IParameterChangeListener) {
			IParameterChangeListener pcl = (IParameterChangeListener) nc;
			pg.internalInitialize(papa, pg.getConversation()); // Update parameters
			pcl.pageParametersChanged(papa); // Send the event to the page
			return true;
		}
		return false;
	}

	static Page createPageWithContent(final IRequestContext ctx, final Constructor< ? extends UrlPage> con, final ConversationContext cc, final PageParameters pp) throws Exception {
		UrlPage nc = createPageContent(ctx, con, cc, pp);
		Page pg = new Page(nc);
		cc.internalRegisterPage(pg, pp);
		return pg;
	}

	/**
	 * FIXME Needs new name
	 * @param ctx
	 * @param con
	 * @param cc
	 * @param pp
	 * @return
	 * @throws Exception
	 */
	static private UrlPage createPageContent(final IRequestContext ctx, final Constructor< ? extends UrlPage> con, final ConversationContext cc, final PageParameters pp) throws Exception {
		//-- Create the page.
		Class< ? >[] par = con.getParameterTypes();
		Object[] args = new Object[par.length];

		for(int i = 0; i < par.length; i++) {
			Class< ? > pc = par[i];
			if(PageParameters.class.isAssignableFrom(pc))
				args[i] = pp;
			else if(ConversationContext.class.isAssignableFrom(pc))
				args[i] = cc;
			else
				throw new IllegalStateException("?? Cannot assign a value to constructor parameter [" + i + "]: " + pc + " of " + con);
		}

		UrlPage p;
		try {
			p = con.newInstance(args);
		} catch(InvocationTargetException itx) {
			Throwable c = itx.getCause();
			if(c instanceof Exception)
				throw (Exception) c;
			else if(c instanceof Error)
				throw (Error) c;
			else
				throw itx;
		}
		return p;
	}

	static public Constructor< ? extends UrlPage> getBestPageConstructor(final Class< ? extends UrlPage> clz, final boolean hasparam) {
		Constructor< ? extends UrlPage>[] car = clz.getConstructors();
		Constructor< ? extends UrlPage> bestcc = null; // Will be set if a conversationless constructor is found
		int score = 0;
		for(Constructor< ? extends UrlPage> cc : car) {
			//-- Check accessibility
			int mod = cc.getModifiers();
			if(!Modifier.isPublic(mod))
				continue;
			Class< ? >[] par = cc.getParameterTypes(); // Zhe parameters
			int sc;
			if((par == null || par.length == 0) && score < 1) {
				sc = 1;
			} else {
				sc = 3; // Better match always
				int cnt = 0;
				int pcnt = 0;
				int nparam = 0; // #of matched constructor parameters
				for(Class< ? > pc : par) {
					if(ConversationContext.class.isAssignableFrom(pc)) {
						cnt++;
						sc += 2;
						nparam++;
					} else if(PageParameters.class.isAssignableFrom(pc)) {
						if(hasparam)
							sc++;
						else
							sc--;
						pcnt++;
						nparam++;
					}
				}
				//-- Skip silly constructors
				if(cnt > 1 || pcnt > 1) {
					WindowSession.LOG.info("Skipping silly constructor: " + cc);
					continue;
				}
				if(nparam != par.length) {
					WindowSession.LOG.info("Not all parameters can be filled-in: " + cc);
					continue;
				}
			}
			if(sc > score) {
				bestcc = cc;
				score = sc;
			}
		}

		//-- At this point we *must* have a usable constructor....
		if(bestcc == null)
			throw new IllegalStateException("The Page class " + clz + " does not have a suitable constructor.");
		return bestcc;
	}

	/**
	 * Finds the best constructor to use for the given page and the given conversation context.
	 *
	 * @param clz
	 * @param ccclz
	 * @param hasparam
	 * @return
	 */
	static public Constructor< ? extends UrlPage> getPageConstructor(final Class< ? extends UrlPage> clz, final Class< ? extends ConversationContext> ccclz, final boolean hasparam) {
		Constructor< ? extends UrlPage> bestcc = null; // Will be set if a conversationless constructor is found
		int score = 0;
		for(Constructor< ? extends UrlPage> cc : clz.getConstructors()) {
			//-- Check accessibility
			int mod = cc.getModifiers();
			if(!Modifier.isPublic(mod))
				continue;
			Class< ? >[] par = cc.getParameterTypes(); // Zhe parameters
			if(par == null || par.length == 0)
				continue; // Never suitable
			boolean acc = false;
			int sc = 5; // This-thingies score: def to 5
			for(Class< ? > pc : par) {
				if(PageParameters.class.isAssignableFrom(pc)) {
					if(hasparam)
						sc++; // This is a good match
					else
						sc--; // Not a good match
				} else if(ccclz.isAssignableFrom(pc)) { // Can accept the specified context?
					acc = true;
				} else { // Unknown parameter type?
					sc = -100;
					break;
				}
			}
			if(!acc) // Conversation not accepted?
				continue;
			if(sc > score) {
				score = sc;
				bestcc = cc;
			}
		}

		//-- At this point we *must* have a usable constructor....
		if(bestcc == null)
			throw new IllegalStateException("The Page class " + clz + " does not have a suitable constructor.");
		return bestcc;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Conversation creation.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Checks to see if the page specified accepts the given conversation class.
	 * @param pgclz
	 * @param ccclz
	 * @return
	 */
	//	static public boolean	pageAcceptsConversation(Class<Page> pgclz, Class<? extends ConversationContext> ccclz) {
	//		Constructor<Page>[]	coar = pgclz.getConstructors();
	//		for(Constructor<Page> c : coar) {
	//			Class<?>[]	par = c.getParameterTypes();
	//			for(Class<?> pc : par) {
	//				if(pc.isAssignableFrom(ccclz))					// This constructor accepts this conversation.
	//					return true;
	//			}
	//		}
	//		return false;
	//	}
	//
	/**
	 * From a page constructor, get the Conversation class to use.
	 *
	 * @param clz
	 * @return
	 */
	static public Class< ? extends ConversationContext> getConversationType(final Constructor< ? extends UrlPage> clz) {
		Class< ? extends ConversationContext> ccclz = null;
		for(Class< ? > pc : clz.getParameterTypes()) {
			if(ConversationContext.class.isAssignableFrom(pc)) {
				//-- Gotcha!! Cannot have 2,
				if(ccclz != null)
					throw new IllegalStateException(clz + ": duplicate conversation contexts in constructor??");
				ccclz = (Class< ? extends ConversationContext>) pc;
			}
		}
		if(ccclz == null)
			return ConversationContext.class;
		return ccclz;
	}
}
