package to.etc.domui.server.parts;

import to.etc.domui.server.*;

public interface IUnbufferedPartFactory extends IPartFactory {
	public void generate(DomApplication app, String rurl, RequestContextImpl param) throws Exception;
}
