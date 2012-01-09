package to.etc.server.servlet;

import javax.servlet.*;

/**
 * This listener can be used to collect statistics for the database pool
 * when to.etc.dbpool's pool manager is used. This collects the URL's
 * used as input and collates database usage statistics per page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 22, 2007
 */
public class StatisticsRequestListener implements ServletRequestListener {
	public void requestDestroyed(ServletRequestEvent ev) {
		//		ServletRequest sr = ev.getServletRequest();
		//		if(!(sr instanceof HttpServletRequest))
		//			return;
		//		HttpServletRequest r = (HttpServletRequest) sr;
		//		PoolManager.getInstance().stopCollecting();
		//		//		System.out.println("RQ-o: "+r.getRequestURL().toString());
	}

	public void requestInitialized(ServletRequestEvent ev) {
		//		ServletRequest sr = ev.getServletRequest();
		//		if(!(sr instanceof HttpServletRequest))
		//			return;
		//		HttpServletRequest r = (HttpServletRequest) sr;
		//		PoolManager.getInstance().startCollecting(r.getRequestURI());
		//		//		System.out.println("RQ-i: "+r.getRequestURL().toString());
	}
}
