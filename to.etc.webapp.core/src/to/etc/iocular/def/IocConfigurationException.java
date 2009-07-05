package to.etc.iocular.def;

import to.etc.iocular.*;

public class IocConfigurationException extends IocException {
	private BasicContainerBuilder m_builder;

	private ComponentBuilder m_cb;

	private String m_location;

	public IocConfigurationException(ComponentBuilder b, String message) {
		super(message);
		m_cb = b;
		m_builder = b.getBuilder();
		m_location = b.getDefinitionLocation();
	}


	public IocConfigurationException(BasicContainerBuilder b, String location, String message) {
		super(message);
		m_builder = b;
		m_location = location;
	}

	public IocConfigurationException(BasicContainerBuilder b, String location, Throwable cause) {
		super(cause);
		m_builder = b;
		m_location = location;
	}

	public IocConfigurationException(BasicContainerBuilder b, String location, String message, Throwable cause) {
		super(message, cause);
		m_builder = b;
		m_location = location;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.getMessage());
		if(m_cb != null) {
			sb.append("\n- The object being built is: ");
			sb.append(m_cb.getIdent());
		}
		if(m_builder != null) {
			sb.append("\n- for the container with the name '");
			sb.append(m_builder.getName());
			sb.append("'");
		}
		if(m_location != null) {
			sb.append("\n- Defined at ");
			sb.append(m_location);
		}

		sb.append("\n\n");
		return sb.toString();
	}

	public String getLocationText() {
		return m_location;
	}
}
