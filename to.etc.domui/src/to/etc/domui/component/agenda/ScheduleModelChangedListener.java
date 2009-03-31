package to.etc.domui.component.agenda;

public interface ScheduleModelChangedListener<T extends ScheduleItem> {
	public void		scheduleItemAdded(T si) throws Exception;
	public void		scheduleItemDeleted(T si) throws Exception;
	public void		scheduleItemChanged(T si) throws Exception;
	public void		scheduleModelChanged() throws Exception;
}
