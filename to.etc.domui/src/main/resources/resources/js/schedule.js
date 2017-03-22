
dojo.declare("WebUI.UISchedule", WebUI.UIControl, {
	initializer: function(id, xtra) {
		WebUI.UISchedule.superclass.initializer(id, xtra);
		this._dragMode = 0;
		this.rounding = 15;		// round 15 minutes
	},

	_reinitVars: function() {
		this._sts = this.date.getTime();			// Start-of-calendar ts
		var d = new Date(this.date);
		if(this.mode == "WEEK") {
			d.setDate(d.getDate()+7);
			this._days = 7;
		} else if(this.mode == "WORKWEEK") {
			d.setDate(d.getDate()+5);
			this._days = 5;
		} else if(this.mode == "DAY") {
			d.setDate(d.getDate()+1);
			this._days = 1;
		} else if(this.mode == "MONTH") {
			d.setMonth(d.getMonth()+1);
			this._days = -1;
		}
		this._end = d;
		this._ets = d.getTime();

		this._root = $(this._id);

		//-- Calc total spread in minutes,
		this._maxMinutes = (this.endHour - this.startHour) * 60;

		//-- Get the header offset,
		var h = $(this._id+"wsh");
		this._headerOffset = h.clientHeight+1;
		h	= $(this._id+"gtr");	// gutter
		this._gutterWidth = h.clientWidth+2;
		h	= $(this._id+"cl");		// half-hour cell
		this._cellWidth = h.clientWidth+1;
		this._cellHeight= h.clientHeight+1;
		this._pxPerHour = this._cellHeight*2;
		this._pxYStart	= h.offsetTop + h.clientHeight+1;
	},

	showing: function(on) {
		if(! on)
			return;
		this._reinitVars();

		//-- Reposition all items
		for(var i = this.items.length; --i >= 0;) {
			var item = this.items[i];
			this.updateItemPos(item, item._div);
		}
	},

	renderLoadComplete: function() {
		this._reinitVars();
		J.disableSelection($(this._id));

		dojo.event.connect(this._root, "onmousedown", this, "timeDown");
		dojo.event.connect(this._root, "onmouseup", this, "timeUp");
		dojo.event.connect(this._root, "onmousemove", this, "timeMove");

		//-- Render all items.
		for(var i = this.items.length; --i >= 0;) {
			var item = this.items[i];
			dojo.debug("item: from "+item.start+" to "+item.end);
			this.renderItem(item, i);
		}
	},

	updateItemPos: function(item, d) {
		//-- Calc position
		var so = this.calcMinuteOffset(item.start, 1);
		var eo = this.calcMinuteOffset(item.end, -1);

		var ys = Math.round(so.min * this._pxPerHour / 60);
		var ye = Math.round(eo.min * this._pxPerHour / 60);

		var xo = this._gutterWidth + (so.day*this._cellWidth);
		d.style.position = "absolute";
		d.style.top =  (ys + this._headerOffset)+"px";
		d.style.left = xo+"px";
		d.style.height = (ye-ys)+"px";
		d.style.width = (this._cellWidth-2)+"px";
	},

	renderItem: function(item) {
		var d = this.makeItemDiv(item);
		this.updateItemPos(item, d);
		var root = $(this._id);
		root.appendChild(d);

		dojo.event.connect(d, "onmousedown", dojo.lang.hitch(this, function(e) { this.apptDown(e, item); }));
//		dojo.event.connect(d, "onmouseup", this, "apptUp");
//		dojo.event.connect(d, "onmousemove", this, "apptMove");
	},

	makeItemDiv: function(item) {
		var d = document.createElement('div');
		item._div = d;
		d.className = "wsitem";
		if(item.type) {
			var s = document.createElement('span');
			d.appendChild(s);
			s.className = 'wsitemtype';
			s.appendChild(document.createTextNode(item.type));
		}
		if(item.img) {
			var s = document.createElement('img');
			d.appendChild(s);
			s.className = 'wsitemimg';
			s.border = '0';
			s.alt = item.name;
			if(item.img.charAt(0) == '/' || item.img.substring(0, 4) == "http")
				s.src = item.img;
			else
				s.src = serverRootURL+item.img;
		}
		if(item.name) {
			var s = document.createElement('span');
			d.appendChild(s);
			s.className = 'wsitemname';
			s.appendChild(document.createTextNode(item.name));
		}
		if(true) {
			var s = document.createElement('span');
			d.appendChild(s);
			s.className = 'wsitemtime';
			s.appendChild(document.createTextNode(Util.strTime(item.start)+" "+Util.strDuration(item.end.getTime()-item.start.getTime())));
		}
		if(item.details) {
			if(item.name)
				d.appendChild(document.createElement('br'));
			var s = document.createElement('span');
			d.appendChild(s);
			s.className = 'wsitemdetails';
			s.appendChild(document.createTextNode(item.details));
		}

		return d;
	},

	/**
	 * Calculates a minutes offset for the date passed. This is an offset
	 * where all "invisible" hours are removed; dividing it hy the #of
	 * visible minutes will return the #of days.
	 * 
	 * @param {Object} date
	 * @param {Object} grav
	 */
	calcMinuteOffset: function(date, grav) {
		var d = new Date(date);
		var ts = d.getTime();		// Get ts in millis
		if(ts <= this._sts)	
			return 0;
		if(ts >= this._ets)
			return this._maxMinutes;

		//-- Is in range. Get a day offset,
		var dayoff = Math.floor( (ts - this._sts) / (86400000) ); 

		//-- Get a minute offset, skipping the invisible hours
		var mins = 0;
		var h = d.getHours();
		if(h < this.startHour) {
			if(grav > 0)
				mins = 0;
			else {
				//-- Round off to end of previous day,
				if(dayoff == 0)
					mins = 0;
				else {
					dayoff--;
					mins = (this.endHour - this.startHour) * 60;
				}
			}
		}
		else if(h >= this.endHour) {
			if(grav > 0) {
				//-- Round to next day,
				if(dayoff+1 >= this._days) {
					mins = this.maxMinutes;
				} else {
					dayoff++;
					mins = 0;
				}
			} else {
				mins = (this.endHour - this.startHour) * 60;
			}
		} else {
			h -= this.startHour;
			mins = h * 60 + d.getMinutes();
		}
		return {day: dayoff, min: mins};
	},

	/**** Appointment dragging ****/

	apptDown: function(e, item) {
		e.stopPropagation();
		if(! this._dragMode || this._dragMode == 0) {
			//-- Select for dragging..
			this._dragItem = item;
			this._dragPos = this.fixPosition(e);
			this._dragOld = {x: item._div.offsetLeft, y:item._div.offsetTop};
			this._dragMode = 1;					// pre-drag
			this.status("Drag DOWN");
		}
	},

	apptMove: function(e) {
		e.stopPropagation();
		if(!this._dragMode || this._dragMode <= 0)
			return;
		var c = this.fixPosition(e);
		var dy = c.y - this._dragPos.y;
		var dx = c.x - this._dragPos.x;
		if(dx < 4 && dx > -4 && dy < 4 && dy > -4)
			return;

		// We're moving allright... Start moving it. We need to apply the "delta" to the item's pos.
		var xo	= this._dragOld.x + dx;
		var	yo 	= this._dragOld.y + dy;
		
		
		var d = this._dragItem._div;
		d.style.top = yo+"px";
		d.style.left = xo+"px";
	},
	apptUp: function(e) {
		e.stopPropagation();
		this.status("Drag UP");
		this._dragMode = 0;
		this.updateItemPos(this._dragItem, this._dragItem._div);
	},

	fixPosition: function(e) {
		var p = dojo.html.getAbsolutePosition(this._root);
		return {x: e.clientX - p.x, y: e.clientY - p.y };
	},

	/**** Empty time dragging ****/
	
	timeReset: function() {
		if(WebUI._timeDiv) {
			$(WebUI._timeDiv).remove();
//			J.destroyNode(this._timeDiv);
			delete WebUI._timeDiv;
		}
		this._timeMode = 0;
	},

	timeDown: function(e) {
		WebUI.timeReset();
		WebUI._timeStart = WebUI.fixPosition(e);
		WebUI._timeMode = 1;
	},
	timeUp: function(e) {
		if(this._dragMode && this._dragMode > 0) {
			this.apptUp(e);
			return;
		}
		this.timeReset();
	},
	roundOff: function(min) {
		return Math.round(min / this.rounding) *this.rounding
	},
	
	timeMove: function(e) {
		if(this._dragMode && this._dragMode > 0) {
			this.apptMove(e);
			return;
		}

		if(! this._timeMode || this._timeMode < 1)
			return;

		var cloc = this.fixPosition(e);
//		dojo.debug("cloc x="+cloc.x+" y="+cloc.y);

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
		var day = Math.floor( (cloc.x - this._gutterWidth) / this._cellWidth);
		var xo = this._gutterWidth + (day*this._cellWidth);

		//-- Calculate a time and duration in minutes for rounding
		var to = (sy - this._headerOffset) / this._pxPerHour* 60;		// Offset in minutes (fractional)
		to	= this.roundOff(to);										// Round off to minutes
		sy	= Math.floor(to * this._pxPerHour / 60 ) + this._headerOffset;	// Calculate rounded Y

		// Calculate an actual time in millis 
		to *= 60 * 1000;		// millis now
		to	+= ((24 * day) + this.startHour) * 60*60*1000;
		to	+= this._sts;
		this._timeDate = new Date(to);

		// get rounded off duration
		var dur = dy *60 / this._pxPerHour;				// Duration in minutes
		dur = this.roundOff(dur);
		if(dur < this.rounding)
			dur = this.rounding;

		// Calulate back end y after roundoff
		ey	= sy + Math.floor(dur * this._pxPerHour/60);
		dur *= 60*1000;

		if(!this._timeDiv) {
			var d = document.createElement('div');
			this._root.appendChild(d);
			d.className = "wsnewtime";
			this._timeDiv = d;
			d.style.position = "absolute";
			d.style.width = (this._cellWidth-2)+"px";
		}
		this._timeDiv.style.top = sy+"px";
		this._timeDiv.style.height = (ey-sy)+"px";
		this._timeDiv.style.left = xo+"px";
		this.status("Time MOVE: y="+sy+", ey="+ey+" xo="+xo+" date "+this._timeDate+", dur="+Util.strDuration(dur));
	},
	
	status: function(s) {
		$('status').innerHTML = s;
	}
	

});
