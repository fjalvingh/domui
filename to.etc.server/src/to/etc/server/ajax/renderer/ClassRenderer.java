package to.etc.server.ajax.renderer;

public class ClassRenderer implements ItemRenderer {
	/**
	 * The list of thingies to call to decode this class.
	 */
	private ClassMemberRenderer[]	m_memberList;

	public ClassRenderer(ClassMemberRenderer[] list) {
		m_memberList = list;
	}

	public void render(ObjectRenderer or, Object val) throws Exception {
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
