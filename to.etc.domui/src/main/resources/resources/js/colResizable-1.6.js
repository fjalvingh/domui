/**
 _ _____           _          _     _
 | |  __ \         (_)        | |   | |
 ___ ___ | | |__) |___  ___ _ ______ _| |__ | | ___
 / __/ _ \| |  _  // _ \/ __| |_  / _` | '_ \| |/ _ \
 | (_| (_) | | | \ \  __/\__ \ |/ / (_| | |_) | |  __/
 \___\___/|_|_|  \_\___||___/_/___\__,_|_.__/|_|\___|

 v1.6 - jQuery plugin created by Alvaro Prieto Lauroba

 Licences: MIT & GPL
 Feel free to use or modify this plugin as far as my full name is kept

 If you are going to use this plug-in in production environments it is
 strongly recommended to use its minified version: colResizable.min.js

 */

(function($) {

	var d = $(document); 		//window object
	var h = $("head");			//head object
	var drag = null;			//reference to the current grip that is being dragged
	var tables = {};			//object of the already processed tables (table.id as key)
	var count = 0;				//internal count to create unique IDs when needed.

	//common strings for packing
	var ID = "id";
	var PX = "px";
	var SIGNATURE = "JColResizer";
	var FLEX = "JCLRFlex";

	//short-cuts
	var I = parseInt;
	var M = Math;
	var ie = navigator.userAgent.indexOf('Trident/4.0') > 0;
	var S;
	try {
		S = sessionStorage;
	} catch(e) {
	}	//Firefox crashes when executed as local file system

	//append required CSS rules
	/*
	 * 20181224 removed .JCLRFlex{width:auto!important;} from here to allow cells to get smaller than their content.
	 */
	h.append("<style type='text/css'>  .JColResizer{table-layout:fixed;} .JColResizer > tbody > tr > td, .JColResizer > tbody > tr > th{overflow:hidden;}  .JCLRgrips{ height:0px; position:relative;} .JCLRgrip{margin-left:-5px; position:absolute; z-index:5; } .JCLRgrip .JColResizer{position:absolute;background-color:red;filter:alpha(opacity=1);opacity:0;width:10px;height:100%;cursor: e-resize;top:0px} .JCLRLastGrip{position:absolute; width:1px; } .JCLRgripDrag{ border-left:1px dotted black;	} .JCLRgrip.JCLRdisabledGrip .JColResizer{cursor:default; display:none;}</style>");

	// below: replaced to allow cells to get smaller than their content in overflow or flex mode.
	// h.append("<style type='text/css'>  .JColResizer{table-layout:fixed;} .JColResizer > tbody > tr > td, .JColResizer > tbody > tr > th{overflow:hidden;}  .JCLRgrips{ height:0px; position:relative;} .JCLRgrip{margin-left:-5px; position:absolute; z-index:5; } .JCLRgrip .JColResizer{position:absolute;background-color:red;filter:alpha(opacity=1);opacity:0;width:10px;height:100%;cursor: e-resize;top:0px} .JCLRLastGrip{position:absolute; width:1px; } .JCLRgripDrag{ border-left:1px dotted black;	} .JCLRFlex{width:auto!important;} .JCLRgrip.JCLRdisabledGrip .JColResizer{cursor:default; display:none;}</style>");
	// replaced h.append("<style type='text/css'>  .JColResizer{table-layout:fixed;} .JColResizer > tbody > tr > td, .JColResizer > tbody > tr > th{overflow:hidden;padding-left:0!important; padding-right:0!important;}  .JCLRgrips{ height:0px; position:relative;} .JCLRgrip{margin-left:-5px; position:absolute; z-index:5; } .JCLRgrip .JColResizer{position:absolute;background-color:red;filter:alpha(opacity=1);opacity:0;width:10px;height:100%;cursor: e-resize;top:0px} .JCLRLastGrip{position:absolute; width:1px; } .JCLRgripDrag{ border-left:1px dotted black;	} .JCLRFlex{width:auto!important;} .JCLRgrip.JCLRdisabledGrip .JColResizer{cursor:default; display:none;}</style>");


	/**
	 * Function to allow column resizing for table objects. It is the starting point to apply the plugin.
	 * @param {DOM node} tb - reference to the DOM table object to be enhanced
	 * @param {Object} options    - some customization values
	 */
	var init = function(tb, options) {
		var t = $(tb);				    //the table object is wrapped
		t.opt = options;                //each table has its own options available at anytime
		t.mode = options.resizeMode;    //shortcuts
		t._disabledColumns = t.opt.disabledColumns;
		if(t.opt.disable) return destroy(t);				//the user is asking to destroy a previously colResized table
		var id = t.id = t.attr(ID) || SIGNATURE + count++;	//its id is obtained, if null new one is generated
		t._isPostbackSafe = t.opt.postbackSafe; 							//short-cut to detect postback safe
		if(!t.is("table") || tables[id] && !t.opt.partialRefresh) return; 		//if the object is not a table or if it was already processed then it is ignored.
		if(t.opt.hoverCursor !== 'e-resize') h.append("<style type='text/css'>.JCLRgrip .JColResizer:hover{cursor:" + t.opt.hoverCursor + "!important}</style>");  //if hoverCursor has been set, append the style
		// t.addClass(SIGNATURE).attr(ID, id).before('<div class="JCLRgrips"/>');	//the grips container object is added. Signature class forces table rendering in fixed-layout mode to prevent column's min-width
		t.attr(ID, id).before('<div class="JCLRgrips"/>');	//the grips container object is added. Signature class forces table rendering in fixed-layout mode to prevent column's min-width
		t._grips = [];
		t._columns = [];
		t._width = t.width();
		t._gripContainer = t.prev();
		t._isFixed = t.opt.fixed;
		t._isFlex = t.opt.flex;
		t._isOverflow = t.opt.overflow;
		if(options.marginLeft) t._gripContainer.css("marginLeft", options.marginLeft);  	//if the table contains margins, it must be specified
		if(options.marginRight) t._gripContainer.css("marginRight", options.marginRight);  	//since there is no (direct) way to obtain margin values in its original units (%, em, ...)
		t._cellSpacing = I(ie ? tb.cellSpacing || tb.currentStyle.borderSpacing : t.css('border-spacing')) || 2;	//table cellspacing (not even jQuery is fully cross-browser)
		t._borderWidth = I(ie ? tb.border || tb.currentStyle.borderLeftWidth : t.css('border-left-width')) || 1;	//outer border width (again cross-browser issues)
		// if(!(tb.style.width || tb.width)) t.width(t.width()); //I am not an IE fan at all, but it is a pity that only IE has the currentStyle attribute working as expected. For this reason I can not check easily if the table has an explicit width or if it is rendered as "auto"
		console.log("before table width=" + t.width());
		t.width(t.width());
		console.log("after table width=" + t.width());
		tables[id] = t; 	//the table object is stored using its id as key
		createGrips(t);		//grips are created
		t.addClass(SIGNATURE);
	};


	/**
	 * This function allows to remove any enhancements performed by this plugin on a previously processed table.
	 * @param {jQuery ref} t - table object
	 */
	var destroy = function(t) {
		var id = t.attr(ID), t = tables[id];		//its table object is found
		if(!t || !t.is("table")) return;			//if none, then it wasn't processed
		t.removeClass(SIGNATURE + " " + FLEX).gc.remove();	//class and grips are removed
		delete tables[id];						//clean up data
	};

	/**
	 * Function to create all the grips associated with the table given by parameters
	 * @param {jQuery ref} t - table object
	 */
	var createGrips = function(t) {
		var th = t.find(">thead>tr:first>th,>thead>tr:first>td"); //table headers are obtained
		if(!th.length) th = t.find(">tbody>tr:first>th,>tr:first>th,>tbody>tr:first>td, >tr:first>td");	 //but headers can also be included in different ways
		th = th.filter(":visible");					//filter invisible columns
		t._columnGroups = t.find("col"); 						//a table can also contain a colgroup with col elements
		t._columnCount = th.length;							//table length is stored
		if(t._isPostbackSafe && S && S[t.id]) memento(t, th);		//if 'postbackSafe' is enabled and there is data for the current table, its coloumn layout is restored
		var totw = 0;
		th.each(function(i) {						//iterate through the table column headers
			var column = $(this); 						//jquery wrap for the current column
			var isDisabledColumn = t._disabledColumns.indexOf(i) != -1;           //is this a disabled column?
			var grip = $(t._gripContainer.append('<div class="JCLRgrip"></div>')[0].lastChild); //add the visual node to be used as grip
			grip.append(isDisabledColumn ? "" : t.opt.gripInnerHtml).append('<div class="' + SIGNATURE + '"></div>');
			if(i === t._columnCount - 1) {                        //if the current grip is the las one
				grip.addClass("JCLRLastGrip");         //add a different css class to stlye it in a different way if needed
				if(t._isFixed) grip.html("");                 //if the table resizing mode is set to fixed, the last grip is removed since table with can not change
			}
			grip.bind('touchstart mousedown', onGripMouseDown); //bind the mousedown event to start dragging

			if(!isDisabledColumn) {
				//if normal column bind the mousedown event to start dragging, if disabled then apply its css class
				grip.removeClass('JCLRdisabledGrip').bind('touchstart mousedown', onGripMouseDown);
			} else {
				grip.addClass('JCLRdisabledGrip');
			}

			grip._table = t;
			grip._index = i;
			grip._column = column;
			column._width = column.width();								//some values are stored in the grip's node data as shortcut
			console.log('w = ' + column._width);
			totw += column._width;

			column._minWidth = column.innerWidth() - column.width();		//FIX issue 45 don't go below total added padding width otherwise skewed results
			t._grips.push(grip);
			t._columns.push(column);										//the current grip and column are added to its table object
			// column.width(column._width).removeAttr("width");				//the width of the column is converted into pixel-based measurements
			column.removeAttr("width");										//the width of the column is converted into pixel-based measurements
			t._columnGroups.eq(i).width(column._width);
			grip.data(SIGNATURE, {index: i, tableId: t.attr(ID), last: i === t._columnCount - 1});	 //grip index and its table name are stored in the HTML
		});
		console.log("Total width = " + totw + ", table width = " + t.width());
		t._columnGroups
			.removeAttr("width")
		;	//remove the width attribute from elements in the colgroup

		t.find('td, th').not(th).not('table th, table td').each(function() {
			$(this).removeAttr('width');	//the width attribute is removed from all table cells which are not nested in other tables and dont belong to the header
		});
		if(!t._isFixed) {
			t.removeAttr('width');
			if(t._isFlex)
				t.addClass(FLEX); //if not fixed, let the table grow as needed
		}
		syncGrips(t); 				//the grips are positioned according to the current table layout
		//there is a small problem, some cells in the table could contain dimension values interfering with the
		//width value set by this plugin. Those values are removed

	};


	/**
	 * Function to allow the persistence of columns dimensions after a browser postback. It is based in
	 * the HTML5 sessionStorage object, which can be emulated for older browsers using sessionstorage.js
	 * @param {jQuery ref} t - table object
	 * @param {jQuery ref} th - reference to the first row elements (only set in deserialization)
	 */
	var memento = function(t, th) {
		console.log("Memento called");
		var w, m = 0, i = 0, aux = [], tw;
		if(th) {										//in deserialization mode (after a postback)
			t._columnGroups.removeAttr("width");
			if(t.opt.flush) {
				S[t.id] = "";
				return;
			} 	//if flush is activated, stored data is removed
			w = S[t.id].split(";");					//column widths is obtained
			tw = w[t._columnCount + 1];
			if(!t._isFixed && tw) {							//if not fixed and table width data available its size is restored
				t.width(tw *= 1);
				if(t.opt.overflow) {						//in overflow mode, restore table width also as table min-width
					t.css('min-width', tw + PX);
					t._width = tw;
				}
			}
			for(; i < t._columnCount; i++) {				//for each column
				aux.push(100 * w[i] / w[t._columnCount] + "%"); 	//width is stored in an array since it will be required again a couple of lines ahead
				th.eq(i).css("width", aux[i]); 	//each column width in % is restored
			}
			for(i = 0; i < t._columnCount; i++)
				t._columnGroups.eq(i).css("width", aux[i]);	//this code is required in order to create an inline CSS rule with higher precedence than an existing CSS class in the "col" elements
		} else {											//in serialization mode (after resizing a column)
			S[t.id] = "";									//clean up previous data
			for(; i < t._columns.length; i++) {				//iterate through columns
				w = t._columns[i].width();					//width is obtained
				S[t.id] += w + ";";							//width is appended to the sessionStorage object using ID as key
				m += w;										//carriage is updated to obtain the full size used by columns
			}
			S[t.id] += m;									//the last item of the serialized string is the table's active area (width),
			//to be able to obtain % width value of each columns while deserializing
			if(!t._isFixed) S[t.id] += ";" + t.width(); 	//if not fixed, table width is stored
		}
	};


	/**
	 * Function that places each grip in the correct position according to the current table layout
	 * @param {jQuery ref} t - table object
	 */
	var syncGrips = function(t) {
		// t._gripContainer.width(t._width);			//the grip's container width is updated
		for(var i = 0; i < t._columnCount; i++) {		//for each column
			var column = t._columns[i];
			// var left = column.offset().left - t.offset().left + column.outerWidth(false) + t._cellSpacing / 2 + PX;
			var left = column.offset().left - t.offset().left;
			if(i < t._columnCount - 1) {
				left += column.outerWidth(false) + t._cellSpacing / 2;
			} else {
				left += column.outerWidth(false) - 5;
			}
			// console.log("left=" + left + PX);

			t._grips[i].css({			//height and position of the grip is updated according to the table layout
				left: left,
				height: t.opt.headerOnly ? t._columns[0].outerHeight(false) : t.outerHeight(false)
			});
		}
	};

	/**
	 * This function updates column's width according to the horizontal position increment of the grip being
	 * dragged. The function can be called while dragging if liveDragging is enabled and also from the onGripDragOver
	 * event handler to synchronize grip's position with their related columns.
	 * @param {jQuery ref} t - table object
	 * @param {number} i - index of the grip being dragged
	 * @param {bool} isOver - to identify when the function is being called from the onGripDragOver event
	 */
	var syncCols = function(t, i, isOver) {
		console.log("syncCols called");
		var inc = drag._prevLeft - drag._left;
		var c = t._columns[i];
		var isLast = i >= t._columnCount - 1;
		var w = c._width + inc;			// proposed with of dragged cell

		if(t._isFixed) {
			/*
			 * Fixed mode: change col's size by changing the next col's size.
			 */
			if(isLast)
				return;

			var c2 = t._columns[i + 1];		// Column just after
			var w2 = c2._width - inc;		// proposed width of cell after, when fixed

			// FIX issue 45
			if(w < c._minWidth) {
				// don't go below total padding width
				w2 -= c._minWidth - w;
				w += c._minWidth - w;
			}
			if(w2 < c2._minWidth && t._isFixed) {
				// don't go below total padding width
				w -= c2._minWidth - w2;
				w2 += c2._minWidth - w2;
			}
			// FIX

			var delta = w - c.width();		// Calculated change in width
			// console.log("dw = " + delta);
			c.width(w + PX);
			t._columnGroups.eq(i).width(w + PX);
			t.width(t._width);				// Table width does not change
			c2.width(w2 + PX);
			t._columnGroups.eq(i + 1).width(w2 + PX);
			if(isOver) {
				c._width = w;
				c2._width = t._isFixed ? w2 : c2._width;
			}
		} else {
			/*
			 * Overflow mode: resize the current column only, increase the size of the table and the start
			 * position of the next column by the delta. This only takes "min-size" constraints into account.
			 */
			if(w < c._minWidth) {
				// don't go below total padding width
				w =c._minWidth;
			}

			var delta = w - c.width();				// Calculated change in width
			// console.log("dw = " + delta);
			c.width(w + PX);
			t._columnGroups.eq(i).width(w + PX);

			t._width += delta;						// Increase table width with delta
			t.width(t._width);
			t.css('min-width', t._width + inc);		//if overflow is set, increment min-width to force overflow (?)
			if(isOver) {
				// console.log("isover not fixed w=" + w);
				c._width = w;
			}
		}
	};

	/**
	 * This function updates all columns width according to its real width. It must be taken into account that the
	 * sum of all columns can exceed the table width in some cases (if fixed is set to false and table has some kind
	 * of max-width).
	 * @param {jQuery ref} t - table object
	 */
	var applyBounds = function(t) {
		var w = $.map(t._columns, function(c) {			//obtain real widths
			return c.width();
		});
		// t.width(t._width = t.width()).removeClass(FLEX);	//prevent table width changes
		// t.width(t._width).removeClass(FLEX);			//force new width for table
		// console.log("tw=" + t._width);
		// $.each(t._columns, function(i,c){
		//     c.width(w[i])._width = w[i];				//set column widths applying bounds (table's max-width)
		// });
		if(t._isFlex)
			t.addClass(FLEX);						//allow table width changes
	};


	/**
	 * Event handler used while dragging a grip. It checks if the next grip's position is valid and updates it.
	 * @param {event} e - mousemove event bound to the window object
	 */
	var onGripDrag = function(e) {
		if(!drag) return;
		var t = drag._table;		//table object reference
		var oe = e.originalEvent.touches;
		var eventX = oe ? oe[0].pageX : e.pageX;    //original position (touch or mouse)
		var x = eventX - drag._dragStartX + drag._left;	        //next position according to horizontal mouse position increment

		var tdx = eventX - drag._dragStartX;		// Total delta
		var ddx = eventX - drag._prevDragX;			// This-drag's delta

		var mw = t.opt.minWidth, i = drag._index;	//cell's min width
		var l = t._cellSpacing * 1.5 + mw + t._borderWidth;
		var last = i === t._columnCount - 1;                 			//check if it is the last column's grip (usually hidden)
		var min = i ? t._grips[i - 1].position().left + t._cellSpacing + mw : l;	//min position according to the contiguous cells
		var max = t._isFixed
			? i === t._columnCount - 1
				? t._width - l
				: t._grips[i + 1].position().left - t._cellSpacing - mw
			: Infinity; 							//max position according to the contiguous cells
		x = M.max(min, M.min(max, x));				//apply bounding
		drag._prevLeft = x;
		drag._prevDragX = eventX;
		drag.css("left", x + PX); 					//apply position increment

		var c = t._columns[drag._index];			//Dragged column
		// if(last) {									//if it is the last grip
		// 	var c = t._columns[drag._index];		//width of the last column is obtained
		// 	drag._width = c._width + x - drag._left;
		// }

		// t._width += dx;
		// console.log("dragx " + drag._prevLeft + ", drag._dragStartX=" + drag._dragStartX + ", ox=" + eventX + ", ox-dragix" + (eventX - drag._dragStartX) + ", dx " + dx + ", t._width " + t._width);
		// console.log("eventX " + eventX + ", d._left=" + drag._left + ", d._startX" + drag._dragStartX + ", tdx " + tdx + ", ddx " + ddx + ", t._width " + t._width);

		if(t.opt.liveDrag) {
			if(last && false) {
				c.width(drag._width);
				if(t._isOverflow) {					//if overflow is set, increment min-width to force overflow
					t.css('min-width', t._width + x - drag._left);
				} else {
					t._width = t.width();
				}
			} else {
				syncCols(t, i, false); 		//columns are synchronized
			}
			syncGrips(t);
			t.width(t._width);						//jal force new width for table

			var cb = t.opt.onDrag;							//check if there is an onDrag callback
			if(cb) {
				e.currentTarget = t[0];
				cb(e);
			}		//if any, it is fired
		}
		return false; 	//prevent text selection while dragging
	};


	/**
	 * Event handler fired when the dragging is over, updating table layout
	 * @param {event} e - grip's drag over event
	 */
	var onGripDragOver = function(e) {
		d.unbind('touchend.' + SIGNATURE + ' mouseup.' + SIGNATURE).unbind('touchmove.' + SIGNATURE + ' mousemove.' + SIGNATURE);
		$("head :last-child").remove(); 					//remove the dragging cursor style
		if(!drag) return;
		drag.removeClass(drag._table.opt.draggingClass);	//remove the grip's dragging css-class
		if(!(drag._prevLeft - drag._left == 0)) {			// if we actually moved
			var t = drag._table;
			var cb = t.opt.onResize; 	    				//get some values
			var i = drag._index;                 			//column index
			var last = i == t._columnCount - 1;         	//check if it is the last column's grip (usually hidden)
			var c = t._grips[i]._column;               		//the column being dragged
			if(last) {
				syncCols(t, i, true);					//the columns are updated
				// c.width(drag._width);
				// c._width = drag._width;
			} else {
				syncCols(t, i, true);	//the columns are updated
			}
			if(!t._isFixed) applyBounds(t);	//if not fixed mode, then apply bounds to obtain real width values
			syncGrips(t);				//the grips are updated
			if(cb) {
				e.currentTarget = t[0];
				cb(e);
			}	//if there is a callback function, it is fired
			if(t._isPostbackSafe && S) memento(t); 	//if postbackSafe is enabled and there is sessionStorage support, the new layout is serialized and stored
		}
		drag = null;   //since the grip's dragging is over
	};

	/**
	 * Event handler fired when the grip's dragging is about to start. Its main goal is to set up events
	 * and store some values used while dragging.
	 * @param {event} e - grip's mousedown event
	 */
	var onGripMouseDown = function(e) {
		var o = $(this).data(SIGNATURE);			//retrieve grip's data
		var t = tables[o.tableId];
		var g = t._grips[o.index];			        //shortcuts for the table and grip objects
		var oe = e.originalEvent.touches;           //touch or mouse event?
		g._dragStartX = oe ? oe[0].pageX : e.pageX;            //the initial position is kept
		t._originalTableWidth = t._width;
		g._left = g.position().left;
		g._prevLeft = g._left;						// The last position calculated at the previous drag event
		g._prevDragX = g._dragStartX;

		d
			.bind('touchmove.' + SIGNATURE + ' mousemove.' + SIGNATURE, onGripDrag)
			.bind('touchend.' + SIGNATURE + ' mouseup.' + SIGNATURE, onGripDragOver);	//mousemove and mouseup events are bound
		h.append("<style type='text/css'>*{cursor:" + t.opt.dragCursor + "!important}</style>"); 	//change the mouse cursor
		g.addClass(t.opt.draggingClass); 	//add the dragging class (to allow some visual feedback)
		drag = g;							//the current grip is stored as the current dragging object
		if(t._columns[o.index]._locked) for(var i = 0, c; i < t._columnCount; i++) {
			c = t._columns[i];
			c._locked = false;
			c._width = c.width();
		} 	//if the column is locked (after browser resize), then c.w must be updated
		return false; 	//prevent text selection
	};


	/**
	 * Event handler fired when the browser is resized. The main purpose of this function is to update
	 * table layout according to the browser's size synchronizing related grips
	 */
	var onResize = function() {
		console.log("resize called");
		for(var t in tables) {
			if(tables.hasOwnProperty(t)) {
				t = tables[t];
				var i, mw = 0;
				t.removeClass(SIGNATURE);   //firefox doesn't like layout-fixed in some cases
				if(t._isFixed) {                  //in fixed mode
					t._width = t.width();        //its new width is kept
					for(i = 0; i < t._columnCount; i++) mw += t._columns[i]._width;
					//cell rendering is not as trivial as it might seem, and it is slightly different for
					//each browser. In the beginning i had a big switch for each browser, but since the code
					//was extremely ugly now I use a different approach with several re-flows. This works
					//pretty well but it's a bit slower. For now, lets keep things simple...
					for(i = 0; i < t._columnCount; i++) t._columns[i].css("width", M.round(1000 * t._columns[i]._width / mw) / 10 + "%")._locked = true;
					//c.l locks the column, telling us that its c.w is outdated
				} else {     //in non fixed-sized tables
					applyBounds(t);         //apply the new bounds
					if(t.mode === 'flex' && t._isPostbackSafe && S) {   //if postbackSafe is enabled and there is sessionStorage support,
						memento(t);                     //the new layout is serialized and stored for 'flex' tables
					}
				}
				syncGrips(t.addClass(SIGNATURE));
			}
		}

	};


	//bind resize event, to update grips position
	$(window).bind('resize.' + SIGNATURE, onResize);


	/**
	 * The plugin is added to the jQuery library
	 * @param {Object} options -  an object that holds some basic customization values
	 */
	$.fn.extend({
		colResizable: function(options) {
			var defaults = {

				//attributes:

				resizeMode: 'fit',                    //mode can be 'fit', 'flex' or 'overflow'
				draggingClass: 'JCLRgripDrag',	//css-class used when a grip is being dragged (for visual feedback purposes)
				gripInnerHtml: '',				//if it is required to use a custom grip it can be done using some custom HTML
				liveDrag: true,				//enables table-layout updating while dragging
				minWidth: 15, 					//minimum width value in pixels allowed for a column
				headerOnly: false,				//specifies that the size of the the column resizing anchors will be bounded to the size of the first row
				hoverCursor: "e-resize",  		//cursor to be used on grip hover
				dragCursor: "e-resize",  		//cursor to be used while dragging
				postbackSafe: false, 			//when it is enabled, table layout can persist after postback or page refresh. It requires browsers with sessionStorage support (it can be emulated with sessionStorage.js).
				flush: false, 					//when postbakSafe is enabled, and it is required to prevent layout restoration after postback, 'flush' will remove its associated layout data
				marginLeft: null,				//in case the table contains any margins, colResizable needs to know the values used, e.g. "10%", "15em", "5px" ...
				marginRight: null, 				//in case the table contains any margins, colResizable needs to know the values used, e.g. "10%", "15em", "5px" ...
				disable: false,					//disables all the enhancements performed in a previously colResized table
				partialRefresh: false,			//can be used in combination with postbackSafe when the table is inside of an updatePanel,
				disabledColumns: [],            //column indexes to be excluded

				//events:
				onDrag: null, 					//callback function to be fired during the column resizing process if liveDrag is enabled
				onResize: null					//callback function fired when the dragging process is over
			};
			var options = $.extend(defaults, options);

			//since now there are 3 different ways of resizing columns, I changed the external interface to make it clear
			//calling it 'resizeMode' but also to remove the "fixed" attribute which was confusing for many people
			options.fixed = true;
			options.overflow = false;
			options.flex = false;
			switch(options.resizeMode) {
				case 'flex':
					options.fixed = false;
					options.flex = true;
					break;
				case 'overflow':
					options.fixed = false;
					options.overflow = true;
					break;
			}

			return this.each(function() {
				init(this, options);
			});
		}
	});
})(jQuery);

