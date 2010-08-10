package to.etc.iocular.def;

import javax.annotation.*;

import to.etc.iocular.*;

public class IocConfigurationException extends IocException {
	@Nullable
	private BasicContainerBuilder m_builder;

	@Nullable
	private ComponentBuilder m_cb;

	@Nullable
	private String m_location;

	public IocConfigurationException(@Nullable ComponentBuilder b, @Nonnull String message) {
		super(message);
		m_cb = b;
		if(b != null) {
			m_builder = b.getBuilder();
			m_location = b.getDefinitionLocation();
		}
	}


	public IocConfigurationException(@Nullable BasicContainerBuilder b, @Nullable String location, @Nonnull String message) {
		super(message);
		m_builder = b;
		m_location = location;
	}

	public IocConfigurationException(@Nullable BasicContainerBuilder b, @Nullable String location, @Nonnull Throwable cause) {
		super(cause);
		m_builder = b;
		m_location = location;
	}

	public IocConfigurationException(@Nullable BasicContainerBuilder b, @Nullable String location, @Nonnull String message, @Nonnull Throwable cause) {
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
