package to.etc.iocular.def;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import to.etc.iocular.container.FailedAlternative;
import to.etc.util.IndentWriter;

public class BuildPlanFailedException extends IocConfigurationException {
	private List<FailedAlternative>		m_list;

	private ComponentBuilder			m_b;

	public BuildPlanFailedException(ComponentBuilder b, String why, List<FailedAlternative> list) {
		super(b, why);
		m_list = list;
		m_b	= b;
	}

	@Override
	public String	getMessage() {
		StringWriter	sw = new StringWriter();
		IndentWriter	iw = new IndentWriter(sw);
		sw.append(super.getMessage()+":\n");
		try {
			iw.print("- The failed object was a ");
			iw.println(m_b.toString());
			if(getLocationText() != null) {
				iw.print("- Defined at ");
				iw.println(getLocationText());
			}
			if(m_list != null && m_list.size() > 0) {
				iw.println("- The failed alternatives were:");
				iw.inc();
				for(FailedAlternative fa : m_list) {
					fa.dump(iw);
				}
				iw.dec();
			}
			sw.close();
		} catch(IOException ioioioio) {
			ioioioio.printStackTrace();
		}
		return sw.getBuffer().toString();
	}
}
