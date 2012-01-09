package to.etc.server.servicer;

import java.io.*;

import to.etc.server.ajax.*;
import to.etc.server.injector.*;

public interface ServiceCallerCallback extends InjectorSourceRetriever {
	public IServiceAuthenticator getAuthenticator();

	//	public boolean	hasUserAnyRole(String[] roles);
	public Writer getResponseWriter(ResponseFormat format, String callname) throws Exception;

	public Object allocateOutput(Class<Object> oc, ResponseFormat rf) throws Exception;

	public void outputCompleted(Object output) throws Exception;
}
