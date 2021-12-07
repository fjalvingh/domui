package to.etc.domui.state;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.server.BrowserVersion;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.util.Constants;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-10-19.
 */
public class RequestContextParameterContainer implements IBasicParameterContainer {
	static private final Logger LOG = LoggerFactory.getLogger(RequestContextParameterContainer.class);

	private final RequestContextImpl m_ctx;

	public RequestContextParameterContainer(RequestContextImpl ctx) {
		m_ctx = ctx;
		String[] parameters = m_ctx.getRequestResponse().getParameters(Constants.PARAM_CONVERSATION_ID);
		if(null != parameters && parameters.length > 1) {
			LOG.error("FATAL MULTIPLE CIDS " + ctx.getRequestResponse().getRequestURI() + " " + ctx.getRequestResponse().getQueryString());
			//throw new IllegalStateException("Multiple CIDs: " + ctx.getRequestResponse().getRequestURI() + " " + ctx.getRequestResponse().getQueryString());
		}

	}

	@Nullable
	@Override
	public String[] getParameterValues(String name) {
		String[] parameters = m_ctx.getRequestResponse().getParameters(name);
		return parameters;
	}

	@Nullable
	@Override
	public String[] getRawUnsafeParameterValues(String name) {
		return m_ctx.getRequestResponse().getRawUnsafeParameters(name);
	}

	@Override
	public int size() {
		return m_ctx.getRequestResponse().getParameterNames().length;
	}

	@Override
	public Set<String> getParameterNames() {
		String[] parameterNames = m_ctx.getRequestResponse().getParameterNames();
		Set<String> res = new HashSet<>();
		for(String parameterName : parameterNames) {
			res.add(parameterName);
		}
		return res;
	}

	@Nullable
	@Override
	public String getUrlContextString() {
		return m_ctx.getUrlContextString();
	}

	@Override
	public int getDataLength() {
		return 0;
	}

	@Nullable
	@Override
	public String getThemeName() {
		return m_ctx.getThemeName();
	}

	@Override
	public String getInputPath() {
		return m_ctx.getInputPath();
	}

	@Override
	public BrowserVersion getBrowserVersion() {
		return m_ctx.getBrowserVersion();
	}
}
