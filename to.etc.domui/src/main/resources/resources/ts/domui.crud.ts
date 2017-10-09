/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.d.ts" />
//import WebUI from "domui.webui.util";

namespace WebUIStatic {
	function handleCalendarChanges(): void {
		// -- find all Calendars
		let cals = $("div.ui-wa").get();
		for(let i = cals.length; --i >= 0;)
			handleCalendar(cals[i]);
	}
	/** ******************* Scheduler component ************************ */
	/**
	 * This handles item placement for a single calendar. It "discovers" the
	 * calendar divs using fixed css class names, and uses "extra" attributes
	 * generated on the root divs for the parts to get the calendar data. This
	 * handles overlapping appointments by dividing a full day lane into
	 * multiple "ways". A day lane starts with one "way". For every appointment
	 * added to a day we check if this appointment overlaps another appointment
	 * on this way[0]. If it does we create a next way (or use it if it already
	 * exists) and check if the appointment clashes there. We do this until we
	 * find a way that can accept the appointment without overlaps. We do this
	 * for all appointments. At the end we have a list of ways for every day. We
	 * now start rendering the appointments by going through them per way, then
	 * per appointment. For every appointment on a way we check if there is an
	 * appointment on a LATER way that overlaps. If not the width of the
	 * appointment includes all LATER ways that do not overlap.
	 *
	 * For this code to work no item may "overlap" a day. The server code takes
	 * care of splitting long appointments into multiple "items" here.
	 */
	function handleCalendar(caldiv) : void {
		// -- TEST calendar object clicky.
		let cal = new WebUI.Agenda(caldiv);
		// $(caldiv).mousedown(function(e) {
		// cal.timeDown(e);
		// });
		cal.loadLayout();
		cal.reposition();
	}
}
