package to.etc.domui.dom;

import java.io.*;

public class FastXmlOutputWriter extends XmlOutputWriterBase implements IBrowserOutput {
	public FastXmlOutputWriter(Writer w) {
		super(w);
	}

	@Override
	public void setIndentEnabled(boolean ind) {}

	@Override
	public boolean isIndentEnabled() {
		return false;
	}
}
