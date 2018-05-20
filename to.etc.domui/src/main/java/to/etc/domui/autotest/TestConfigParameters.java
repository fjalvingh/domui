package to.etc.domui.autotest;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.server.ConfigParameters;

import java.io.File;
import java.util.Map;

public class TestConfigParameters implements ConfigParameters {
	@NonNull
	private final File m_webResources;

	@NonNull
	private final Map<String, String> m_parameters;

	public TestConfigParameters(@NonNull File webResources, @NonNull Map<String, String> parameters) {
		m_webResources = webResources;
		m_parameters = parameters;
	}

	@Override
	@Nullable
	public String getString(@NonNull String name) {
		return m_parameters.get(name);
	}

	@Override
	@NonNull
	public File getWebFileRoot() {
		return m_webResources;
	}

}
