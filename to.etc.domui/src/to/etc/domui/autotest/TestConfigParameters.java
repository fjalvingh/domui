package to.etc.domui.autotest;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.server.*;

public class TestConfigParameters implements ConfigParameters {
	@Nonnull
	private final File m_webResources;

	@Nonnull
	private final Map<String, String> m_parameters;

	public TestConfigParameters(@Nonnull File webResources, @Nonnull Map<String, String> parameters) {
		m_webResources = webResources;
		m_parameters = parameters;
	}

	@Override
	@Nullable
	public String getString(@Nonnull String name) {
		return m_parameters.get(name);
	}

	@Override
	@Nonnull
	public File getWebFileRoot() {
		return m_webResources;
	}

}
