package to.etc.domui.log;

import to.etc.domui.dom.html.*;

public abstract class LoggerConfigPartBase extends Div {

	protected LoggerConfigPartBase() {
		super();
	}

	public abstract boolean validateChanges();

	public abstract boolean saveChanges() throws Exception;

	public abstract String getPartTitle();
}
