package to.etc.iocular.def;

/**
 * Class instance definition.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 27, 2007
 */
public class InstanceDef {
	/** The registered instance */
	private Object m_instance;

	public InstanceDef(Object inst) {
		m_instance = inst;
	}

	public Object getInstance() {
		return m_instance;
	}
}
