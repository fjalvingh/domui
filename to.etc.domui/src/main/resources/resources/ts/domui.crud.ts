/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
// <reference path="domui.webui.d.ts" />
//import WebUI from "domui.webui.util";

namespace WebUI {
	export function handleCalendarChanges(): void {
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
	export function handleCalendar(caldiv) : void {
		// -- TEST calendar object clicky.
		let cal = new Agenda(caldiv);
		// $(caldiv).mousedown(function(e) {
		// cal.timeDown(e);
		// });
		cal.loadLayout();
		cal.reposition();
	}

	class Agenda {
		_rootdiv: HTMLElement;

		_dragMode: number;

		_rounding: number;

		_date : Date;

		_days : number;

		_startHour : number;

		_endHour : number;

		_maxMinutes : number;

		private _headerHeight: number;

		private _gutterWidth: number;

		private _cellWidth: number;

		private _pxPerHour: number;

		private _endDate: Date;

		private _dayMap: any[];

		private _itemdivs: any[];

		private _timeStart: { x: number; y: number };

		private _timeMode: number;

		private _timeDate: Date;

		private _timeDuration: number;

		private _timeDiv: HTMLDivElement;

		constructor(div) {
			this._rootdiv = div;
			this._dragMode = 0;
			this._rounding = 15;		// round 15 minutes
			var cal = this;
			//	$(div).mousemove(function(e) {
			//		cal.timeMove(e);
			//	});
			//	$(div).mouseup(function(e) {
			//		cal.timeUp(e);
			//	});
			//	$(caldiv).mousedown(function(e) {
			//		cal.timeDown(e);
			//	});
			div.onmousedown = function(e) {
				cal.timeDown(e);
			};
			div.onmousemove = function(e) {
				cal.timeMove(e);
			};
			div.onmouseup = function(e) {
				cal.timeUp(e);
			};
		}

		/**
		 * Decode a year,month,day,hour,minute date string.
		 */
		decodeDate(s) {
			var ar = s.split(",");
			if(ar.length != 5)
				alert('Invalid date input: ' + s);
			var d = new Date(parseInt(ar[0]), parseInt(ar[1]) - 1, parseInt(ar[2]), parseInt(ar[3]), parseInt(ar[4]), 0);
			return d;
		}

		/**
		 * Get all data for this control from the div.
		 * @return
		 */
		loadLayout() {
			var caldiv = this._rootdiv;
			//	var s = parseInt(caldiv.getAttribute('startDate'));
			//	this._date		= new Date(s);
			this._date = this.decodeDate(caldiv.getAttribute('startDate'));
			this._days = parseInt(caldiv.getAttribute('days'));
			this._startHour = parseInt(caldiv.getAttribute('hourstart'));
			this._endHour = parseInt(caldiv.getAttribute('hourend'));
			this._maxMinutes = (this._endHour - this._startHour) * 60;

			//-- First find the background table height. This is on the table in IE (tbody has size 0, sigh) and on tbody in firefox
			var tblheight;
			var tbl;
			if($.browser.msie) {
				tbl = $("table.ui-wa-bgtbl", this._rootdiv).get()[0];
				tblheight = tbl.clientHeight;
				tbl = $(".ui-wa-bgtbl tbody", this._rootdiv).get()[0];

			} else {
				tbl = $(".ui-wa-bgtbl tbody", this._rootdiv).get()[0];
				tblheight = tbl.clientHeight;
			}
			var tr = undefined;
			for(var i = 0; i < tbl.childNodes.length; i++) {
				tr = tbl.childNodes[i];
				if(tr.tagName == 'tr' || tr.tagName == 'TR')
					break;
			}
			var td = undefined;
			var ix = 0;
			for(var i = 0; i < tr.childNodes.length; i++) {
				td = tr.childNodes[i];
				if(td.tagName == 'td' || td.tagName == 'TD') {
					if(ix == 0) {
						this._headerHeight = tr.clientHeight + 1;
						this._gutterWidth = td.clientWidth + 1;
					} else if(ix == 1) {
						this._cellWidth = td.clientWidth + 1;
					} else
						break;
					ix++;
				}
			}

			this._pxPerHour = (tblheight - this._headerHeight + 1) / (this._endHour - this._startHour);
			this._endDate = new Date(this._date.getTime());
			this._endDate.setDate(this._endDate.getDate() + this._days);
			this._dayMap = [];
			//	alert('layout: tbl.height='+tblheight+", hdrheight="+this._headerHeight+", endhour="+this._endHour+", starthour="+this._startHour+", pxperhr="+this._pxPerHour);

			/*
			 * Loop 1: locale all items, and assign them to their appropriate "day" and "way".
			 */
			this._itemdivs = $("div.ui-wa-it", this._rootdiv).get();

			//	var b = document.createElement('div');
			//	b.appendChild(document.createTextNode('CLICKER'));
			//	document.body.appendChild(b);
			//	b.onclick() {
			//		var d = $("table.ui-wa-bgtbl").get()[0];
			//
			//		alert('click: tbl.height='+d.clientHeight);
			//	}
		}

		/**
		 * Recalculate all positions of items.
		 * @return
		 */
		reposition() {
			for(var i = this._itemdivs.length; --i >= 0;) {
				//-- Get the item's start and end date,
				var idiv = this._itemdivs[i];
				var sd = this.decodeDate(idiv.getAttribute('startdate'));
				var ed = this.decodeDate(idiv.getAttribute('enddate'));
				//		var sd	= new Date(parseInt(idiv.getAttribute('startdate')));
				//		var ed	= new Date(parseInt(idiv.getAttribute('enddate')));
				this.assignDayAndLane(idiv, sd, ed);
			}

			/*
			 * All items have been assigned their way. Now render them, day by day, way by way.
			 */
			var dayxo = this._gutterWidth;
			for(var i = 0; i < this._dayMap.length; i++) {		// Every day,
				var day = this._dayMap[i];
				if(day == undefined)
					continue;
				var maxlanes = day.ways.length;					// The #of ways used by this day,
				if(maxlanes == 0)
					continue;

				//-- Walk all ways
				for(var wayix = 0; wayix < maxlanes; wayix++) {
					//-- Walk all items in *this* way && assign location.
					var way = day.ways[wayix];					// The current way,
					var wxo = dayxo + wayix * (this._cellWidth / maxlanes);	// Calculate an X offset for all items here,

					for(var iix = 0; iix < way.length; iix++) {
						var item = way[iix];
						var spanlanes = this.calcSpanLanes(day, wayix + 1, item);	// How many lanes may this span?
						var width = this._cellWidth / maxlanes * spanlanes;

						//-- Position!!
						var d = item.div;
						d.style.position = "absolute";
						d.style.top = (item.ys + this._headerHeight) + "px";
						d.style.left = wxo + "px";
						d.style.height = (item.ye - item.ys) + "px";
						d.style.width = (width - 2) + "px";
						d.style.display = 'block';
						//$(d).show('pulsate', {times: 3}, 3000);
					}
				}

				//-- Next day
				dayxo += this._cellWidth;
			}
		}

		assignDayAndLane(idiv, sd, ed) {
			//-- Calc positions and create the initial "item"
			var so = this.calcMinuteOffset(sd, 1);
			var eo = this.calcMinuteOffset(ed, -1);
			var day = this._dayMap[so.day];
			if(day == undefined)
				day = this._dayMap[so.day] = {day: so.day, ways: [[]]};

			var ys = Math.round(so.min * this._pxPerHour / 60);
			var ye = Math.round(eo.min * this._pxPerHour / 60);

			var item = {} as any;
			item.day = so.day;
			item.ys = ys;
			item.ye = ye;
			item.div = idiv;

			//-- Start item placement over the ways,
			for(var i = 0; i < 4; i++) {
				var way = day.ways[i];
				if(way == undefined)
					way = day.ways[i] = [];// Add another way.
				if(this.placeOnWay(way, item))
					return;
			}

			//-- Cannot be placed on 4 ways-> overflow; make it overlap.
			day.ways.push(item);
		}

		/**
		 * Calculate the #of ways AFTER the item's own way that the item can occupy without overlap.
		 */
		calcSpanLanes(day, start, item) {
			var nways = 1;							// Always occupies it's own lane.
			for(var i = start; i < day.ways.length; i++) {
				var way = day.ways[i];				// Next way;
				for(var j = way.length; --j >= 0;) {	// For all items @ this way
					var oi = way[j];					// Other item,
					if(this.calItemOverlaps(item, oi))
						return nways;					// Overlaps -> cannot occupy this way.
				}
				nways++;
			}
			return nways;
		}

		/**
		 * Try to place this item on this way. Succeeds when there is no overlap.
		 */
		placeOnWay(way, item) {
			for(var i = way.length; --i >= 0;) {
				var oi = way[i];					// Other item,
				if(this.calItemOverlaps(item, oi))	// Has an overlap?
					return false;					// Cannot be placed here,
			}

			//-- No overlap -> add here,
			way.push(item);
			return true;
		}

		calItemOverlaps(i1, i2) {
			return i1.ys < i2.ye && i1.ye > i2.ys;
		}

		/**
		 * Calculates a minutes offset for the date passed. This is an offset
		 * where all "invisible" hours are removed; dividing it hy the #of
		 * visible minutes will return the #of days.
		 *
		 * @param {Object} date
		 * @param {Object} grav
		 */
		calcMinuteOffset(d, grav) {
			var ts = d.getTime();		// Get ts in millis
			if(ts <= this._date.getTime())
				return {day: 0, min: 0};		// Starts before start-> return start
			if(ts >= this._endDate.getTime())
				return {day: (this._days - 1), min: this._maxMinutes};

			//-- Is in range. Get a day offset,
			var dayoff = Math.floor((ts - this._date.getTime()) / (86400000));
			//	alert('dayoff = '+dayoff+", hour="+d.getHours()+", d="+d);

			//-- Get a minute offset, skipping the invisible hours
			var mins = 0;
			var h = d.getHours();
			if(h < this._startHour) {
				if(grav > 0)
					mins = 0;
				else {
					//-- Round off to end of previous day,
					if(dayoff == 0)
						mins = 0;
					else {
						dayoff--;
						mins = (this._endHour - this._startHour) * 60;
					}
				}
			}
			else if(h >= this._endHour) {
				if(grav > 0) {
					//-- Round to next day,
					if(dayoff + 1 >= this._days) {
						mins = this._maxMinutes;
					} else {
						dayoff++;
						mins = 0;
					}
				} else {
					mins = (this._endHour - this._startHour) * 60;
				}
			} else {
				h -= this._startHour;
				mins = h * 60 + d.getMinutes();
			}
			return {day: dayoff, min: mins};
		}


		/************** Calendar new appointment dragging *********************/
		destroyNode(x) {
			$(x).remove();
		}

		timeDown(e) {
			this.timeReset();
			this._timeStart = this.fixPosition(e);
			this._timeMode = 1;
		}

		timeReset() {
			if(this._timeDiv) {
				this.destroyNode(this._timeDiv);
				this._timeDiv = undefined;
			}
			this._timeMode = 0;
		}

		timeUp(e) {
			if(this._dragMode && this._dragMode > 0) {
				// this.apptUp(e);		FIXME THIS IS MISSING
				return;
			}
			if(this._timeMode && this._timeMode > 0) {
				this.timeReset();
				var fields = {} as any;
				fields.date = this._timeDate.getTime();
				fields.duration = this._timeDuration;
				WebUI.scall(this._rootdiv.id, 'newappt', fields);
				return;
			}
			this.timeReset();
		}

		roundOff(min) {
			return Math.round(min / this._rounding) * this._rounding;
		}

		timeMove(e) {
			if(this._dragMode && this._dragMode > 0) {
				// this.apptMove(e);			FIXME THIS IS MISSING
				return;
			}

			if(!this._timeMode || this._timeMode < 1)
				return;

			var cloc = this.fixPosition(e);
			//	dojo.debug("cloc x="+cloc.x+" y="+cloc.y);

			var dy = Math.abs(cloc.y - this._timeStart.y);
			var dx = Math.abs(cloc.x - this._timeStart.x);
			if(dx < 4 && dy < 4)
				return;

			//-- Determine start loc
			var sy = this._timeStart.y;
			var ey = cloc.y;
			if(sy > ey) {
				ey = this._timeStart.y;
				sy = cloc.y;
			}
			var dy = ey - sy;

			//-- Force x to the start of a day
			var day = Math.floor((cloc.x - this._gutterWidth) / this._cellWidth);
			var xo = this._gutterWidth + (day * this._cellWidth);

			//-- Calculate a time and duration in minutes for rounding
			var to = (sy - this._headerHeight) / this._pxPerHour * 60;		// Offset in minutes (fractional)
			to = this.roundOff(to);										// Round off to minutes
			sy = Math.floor(to * this._pxPerHour / 60) + this._headerHeight;	// Calculate rounded Y

			// Calculate an actual time in millis
			to *= 60 * 1000;		// millis now
			to += ((24 * day) + this._startHour) * 60 * 60 * 1000;
			to += this._date.getTime();
			this._timeDate = new Date(to);

			// get rounded off duration
			var dur = dy * 60 / this._pxPerHour;				// Duration in minutes
			dur = this.roundOff(dur);
			if(dur < this._rounding)
				dur = this._rounding;

			// Calulate back end y after roundoff
			ey = sy + Math.floor(dur * this._pxPerHour / 60);
			dur *= 60 * 1000;
			this._timeDuration = dur;

			if(!this._timeDiv) {
				var d = document.createElement('div');
				this._rootdiv.appendChild(d);
				d.className = "ui-wa-nt";
				this._timeDiv = d;
				d.style.position = "absolute";
				d.style.width = (this._cellWidth - 2) + "px";
				d.style.zIndex = "90";
				// console.debug("dur="+dur+", ey="+ey+", sy="+sy+", xo="+xo+",
				// w="+d.style.width);
			}
			this._timeDiv.style.top = sy + "px";
			this._timeDiv.style.height = (ey - sy) + "px";
			this._timeDiv.style.left = xo + "px";
			//	this.status("Time MOVE: y="+sy+", ey="+ey+" xo="+xo+" date "+this._timeDate+", dur="+Util.strDuration(dur));
		}

		fixPosition(e) {
			var p = WebUI.getAbsolutePosition(this._rootdiv);
			return {x: e.clientX - p.x, y: e.clientY - p.y};
		}

	}
}
