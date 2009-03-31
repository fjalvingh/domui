package to.etc.domui.component.agenda;

import java.util.Date;
import java.util.List;

/**
 * Model for the schedule component. Each schedule component must have a model
 * which returns data when the schedule needs it.
 * <strong>Important</strong> None of the calls here must buffer it's contents!! All controls that
 * use this model will only ask for a given dataset <i>once</i> and cache the response locally. Only
 * when a control's display format or date range changes will it re-query the contents of this
 * model!! 
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 27, 2007
 */
public interface ScheduleModel<T extends ScheduleItem> {
	/**
	 * Get schedule items in the specified period. This gets called only once as long as the
	 * component's date range does not change. <b>This means that this call should not cache
	 * it's response to this call</b>.
	 *
	 * @param start
	 * @param end
	 * @return
	 * @throws Exception
	 */
	public List<T>					getScheduleItems(Date start, Date end) throws Exception;

	/**
	 * Returns a list of holidays in the given period. Each holiday can be a single day only; the
	 * date it contains gets time-truncated.
	 * This gets called only once as long as the component's date range does not change. <b>This 
	 * means that this call should not cache it's response to this call</b>.
	 * @param start
	 * @param end
	 * @return
	 * @throws Exception
	 */
	public List<ScheduleHoliday>	getScheduleHolidays(Date start, Date end) throws Exception;

	/**
	 * Returns the work hours for the user on each day in the given period. This call <b>must</b>
	 * return WorkHour's where each hour's start and end date is fully filled with an actual
	 * date range falling fully on a single day. To specify a lunch period return two WorkHour
	 * periods for a day.
	 * This call may just return an empty list if no work hour display logic is wanted.
	 * @param start
	 * @param end
	 * @return
	 * @throws Exception
	 */
	public List<ScheduleWorkHour>	getScheduleWorkHours(Date start, Date end) throws Exception;

	public void	addScheduleListener(ScheduleModelChangedListener<T> chl);
	
	public void	removeScheduleListener(ScheduleModelChangedListener<T> chl);
}
