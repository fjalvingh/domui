package to.etc.server.servlet.cmd;

public interface ICommandServletHandler {
	public String getExtentionName();

	public void initialize(CommandServletContext ctx);

	public void close();
}
