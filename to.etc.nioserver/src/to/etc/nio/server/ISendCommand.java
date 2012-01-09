package to.etc.nio.server;

/**
 * A handler which causes a command to be prepared for
 * the NIO server. The command must cause data to be
 * written to any of the output parts passed to it. Since
 * all data passed gets buffered one needs to take care
 * on how much data is to be passed: it is better to
 * pass a chunk of max. 8KB per call repeatedly instead of
 * returning a 20MB chunk in one go.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 3, 2006
 */
public interface ISendCommand {
	public void sendCompleted() throws Exception;

	public void sendAborted(Throwable t) throws Exception;

	public boolean prepareData(NioOutputStream nos) throws Exception;
}
