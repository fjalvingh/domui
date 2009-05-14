package to.etc.iocular.container;

import to.etc.iocular.IocException;

public class IocContainerException extends IocException {
	private BasicContainer		m_c;

	public IocContainerException(BasicContainer c, String why) {
		super(why);
		m_c = c;
	}

	@Override
	public String getMessage() {
		return super.getMessage()+" (container "+m_c.getIdent()+")";
	}
}
