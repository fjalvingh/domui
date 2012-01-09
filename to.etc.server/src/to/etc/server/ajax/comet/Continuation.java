package to.etc.server.ajax.comet;

public interface Continuation {
	public void resume();

	public void setTimeout(long ms);
}
