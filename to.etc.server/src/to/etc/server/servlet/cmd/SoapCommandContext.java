package to.etc.server.servlet.cmd;

import java.io.*;

import org.w3c.dom.*;

import to.etc.xml.*;

public class SoapCommandContext extends CommandContext {
	/** This contains the command's node */
	private Node	m_node;

	public SoapCommandContext(CommandServletContext ctx, Node node, String name) throws IOException {
		super(ctx, name);
		m_node = node;
	}

	@Override
	public String getStringParam(String name, String def) throws Exception {
		return DomTools.stringNode(m_node, name, def);
	}

	public Node getBody() {
		return m_node;
	}
}
