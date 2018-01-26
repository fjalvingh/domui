/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />
//import WebUI from "domui.webui.util";

namespace WebUI {
	/**
	 * This adds a "resize" listener to the window, and every window "resize" it will call a method
	 * to recalculate the height of a single div (flexid) based on the position of:
	 * <ul>
	 *	<li>The 'bottom' position of the element just above it (get it's top position and add it's height)</li>
	 *	<li>A bottom 'offset', currently an integer in pixels. It will become a "bottom item" id later, so we can auto-size a div "sandwiched" between two other elements.</li>
	 * </ul>
	 * The recalculate code will be called every time the window resizes.
	 *
	 * @param topid
	 * @param flexid
	 * @param bottom
	 */
	export function autoHeightReset(topid, flexid, bottom) {
		$(window).bind("resize", function() {
			recalculateAutoHeight(topid, flexid, bottom);
		});
		recalculateAutoHeight(topid, flexid, bottom);
	}

	export function recalculateAutoHeight(topid, flexid, bottom) {
		try {
			var tbot = $(topid).offset().top + $(topid).height();
			var height = $(window).height() - tbot - bottom;
			$(flexid).height(height+"px");
		} catch(x) {
			//-- Ignore for now
		}
	}

	export function setThreePanelHeight(top, middle, bottom) {
//		try {
		middle = "#"+middle;
		var height = $(middle).parent().height();		// Assigned height of the container

		var theight = 0;
		if(typeof top === "string") {
			theight = $("#"+top).height();					// Get the end of the "top" element
		} else if(top) {
			theight = top;
		}

		//-- Get height of bottom, if present
		var bheight = 0;
		if(typeof bottom === "string") {
			bheight = $("#"+bottom).height();
		} else if(bottom) {
			bheight = $(bottom).height();
		}
		height -= theight - bheight;
		if(height < 0) {
			height = 0;
		}
		$(middle).height(height+"px");
		$(middle).css({"overflow-y": "auto"});

	}

	export function autoHeight(flexid, bottom) {
		$(window).bind("resize", function() {
			autoHeightRecalc(flexid, bottom);
		});
		autoHeightRecalc(flexid, bottom);
	}

	export function autoHeightRecalc(flexid, bottom) {
		var tbot = $(flexid).offset().top;
		var height = $(window).height() - tbot - bottom;
		$(flexid).height(height + "px");
	}

	export function notifySizePositionChangedOnId(elemId : string) : void {
		var element = document.getElementById(elemId);
		if (!element){
			return;
		}
		// Send back size information to server
		var fields = {};
		// fields["webuia"] = "notifyClientPositionAndSize";
		fields[element.id + "_rect"] = $(element).position().left + "," + $(element).position().top + "," + $(element).width() + "," + $(element).height();
		fields["window_size"] = window.innerWidth + "," + window.innerHeight;
		WebUI.scall(element.id, "notifyClientPositionAndSize", fields);
	}

	export function notifySizePositionChanged(event, ui) {
		var element = ui.helper.get(0);
		if (!element){
			return;
		}
		notifySizePositionChangedOnId(element.id);
	}

	export function floatingDivResize(ev, ui) : void {
		$(ui.helper.get(0)).css('position', 'fixed');
		$('[stretch=true]').doStretch();
		$('.ui-dt, .ui-fixovfl').fixOverflow();
	}

	export function onWindowResize() : void {
		WebUI.doCustomUpdates();
	}

}
