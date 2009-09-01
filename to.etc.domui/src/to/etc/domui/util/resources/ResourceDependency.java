package to.etc.domui.util.resources;

final class ResourceDependency {
	private IModifyableResource m_resource;

	private long m_timestamp;

	public ResourceDependency(IModifyableResource resource) {
		m_resource = resource;
		m_timestamp = resource.getLastModified();
	}

	public boolean isModified() {
		return m_timestamp != m_resource.getLastModified();
	}
}
