package to.etc.webapp.ajax.comet;

public interface Continuation {
	public void resume();

	public void setTimeout(long ms);
}
