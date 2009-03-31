package to.etc.domui.component.delayed;

/**
 * Progress indicator interface.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 7, 2008
 */
public interface IProgress {
	/**
	 * Define the total amount of work we expect to do.
	 * @param work
	 */
	public void			setTotalWork(int work);

	/**
	 * Define how much of the work has completed, currently.
	 * @param work
	 */
	public void			setCompleted(int work);

	/**
	 * Indicate a cancel request.
	 */
	public void			cancel();

	public boolean		isCancelled();
}
