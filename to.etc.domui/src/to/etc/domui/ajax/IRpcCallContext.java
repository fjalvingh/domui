package to.etc.domui.ajax;

import java.io.*;

import to.etc.domui.annotations.*;

public interface IRpcCallContext {
	public <T> T	createHandlerClass(Class<T> clz) throws Exception;

//	public IServiceAuthenticator	getAuthenticator();
	public boolean	hasRight(String roles);
	public Writer	getResponseWriter(ResponseFormat format, String callname) throws Exception;
	public <T> T	allocateOutput(Class<T> oc, ResponseFormat rf) throws Exception;
	public void		outputCompleted(Object output) throws Exception;
}
