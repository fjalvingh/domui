package to.etc.webapp.ajax.renderer;

public class ClassRenderer implements ItemRenderer {
	/**
	 * The list of thingies to call to decode this class.
	 */
	private final ClassMemberRenderer[] m_memberList;

	public ClassRenderer(final ClassMemberRenderer[] list) {
		m_memberList = list;
	}

	public void render(final ObjectRenderer or, final Object val) throws Exception {
		or.renderObjectStart(val);
		int count = 0;
		for(ClassMemberRenderer r : m_memberList) {
			//			or.get.println("\n["+r.getName()+"]: "+r.getMethod().toGenericString());
			//			System.out.println("ClassRenderer: field "+r.getMethod().getName());
			count = r.render(or, val, count);
		}
		or.renderObjectEnd(val);
	}
}
