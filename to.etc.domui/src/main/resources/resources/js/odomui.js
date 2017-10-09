$(function(){
	$.getScript("$js/domui-date-checker.js");
});

function _block() {
	WebUI.blockUI();
}
function _unblock() {
	WebUI.unblockUI();
}
$(document).ajaxStart(_block).ajaxStop(_unblock);
$(window).bind('beforeunload', function() {
	WebUI.beforeUnload();
	return undefined;
});

//-- calculate browser major and minor versions
{
	try {
		var v = $.browser.version.split(".");
		$.browser.majorVersion = parseInt(v[0], 10);
		$.browser.minorVersion = parseInt(v[1], 10);

		//-- And like clockwork MS fucks up with IE 11: it no longer registers as msie. Fix that here.
		if(navigator.appName == 'Netscape') {
			var ua = navigator.userAgent;
			if(ua.indexOf("Trident/") != -1)
				$.browser.msie = true;
		}

		if (/Edge/.test(navigator.userAgent)) {
			$.browser.ieedge = true;
		}
	} catch(x) {}

//	alert('bmaj='+$.browser.majorVersion+", mv="+$.browser.minorVersion);
}

/** WebUI helper namespace */
var WebUI;
if(WebUI === undefined)
	WebUI = new Object();

$.extend(WebUI, {
	returnKeyPress : function(evt, node) {
		var keyCode = WebUI.normalizeKey(evt);
		if (keyCode != 13000 && keyCode != 13)
			return true;
		// Be sure 'onblur' is executed before next action (like submit)
		try {
			evt.target.onblur(evt);
		} catch (err) {
			// Ignore any error in case that target does not support onblur...
		}
		WebUI.scall(evt.currentTarget ? evt.currentTarget.id : node.id, 'returnpressed');
		return false;
	},

	wtMouseDown : function(e) {
		alert(e);
	},

	oddCharAndClickCallback : function(nodeId, clickId) {
		WebUI.oddChar(document.getElementById(nodeId));
		document.getElementById(clickId).click();
	},
	oddChar : function(obj) {
		WebUI.toClip(obj.innerHTML);
	},





	_selectStart : undefined,


	/** ************ Drag-and-drop support code ****************** */
	/**
	 * When mouse is downed on an item that is draggable. This moves to PREDRAG
	 * mode where the first move will create a visible representation of this
	 * node, ready for dropping.
	 */
	dragMouseDown : function(item, evt) {
		WebUI.dragReset();
		WebUI._dragType = item.getAttribute('uitype');
		if (!WebUI._dragType)
			alert("This DRAGGABLE node has no 'uitype' attribute??");
		var dragAreaId = item.getAttribute('dragarea');
		if (dragAreaId){
			WebUI._dragNode  = document.getElementById(dragAreaId);
		}else
			WebUI._dragNode = item;
		WebUI._dragMode = 1; // PREDRAG
		$(document.body).bind("mousemove", WebUI.dragMouseMove);
		$(document.body).bind("mouseup", WebUI.dragMouseUp);
		var apos = WebUI.getAbsolutePosition(item);
		WebUI._dragSourceOffset = apos;
		apos.x = evt.clientX - apos.x;
		apos.y = evt.clientY - apos.y;
		if(evt.preventDefault)
			evt.preventDefault(); // Prevent ffox image dragging
		else{
			evt.returnValue = false;
		}
		if(document.attachEvent){
			document.attachEvent( "onselectstart", WebUI.preventSelection);
		}
	},

	dragMouseUp : function() {
		// -- If we're in DRAGGING mode we may accept the drop
		try {
			if (WebUI._dragMode == 2) {
				WebUI.dragClearTimer();
				var dz = WebUI.dropTargetFind(WebUI._dragLastX,
					WebUI._dragLastY);
				if (dz) {
					WebUI.dropClearZone(); // Discard any dropzone visuals
					dz._drophandler.drop(dz);
				}else{
					WebUI._dragNode.style.display='';//no drop zone, so restore the dragged item
				}
			}
		} finally {
			WebUI.dragReset();
		}
	},

	dragMouseMove : function(e) {
		if (WebUI._dragMode == 0) {
			WebUI.dragReset();
			return;
		}

		if (WebUI._dragMode == 1) {
			// -- preDRAG mode: create the node copy, then move it to the
			// offset' location.
			WebUI._dragCopy = WebUI.dragCreateCopy(WebUI._dragNode);
			//MVE make this optional.
			WebUI._dragNode.style.display='none';

			WebUI._dragMode = 2;
			document.body.appendChild(WebUI._dragCopy);
		}
		WebUI._dragCopy.style.top = (e.clientY - WebUI._dragSourceOffset.y)
			+ "px";
		WebUI._dragCopy.style.left = (e.clientX - WebUI._dragSourceOffset.x)
			+ "px";
		// console.debug("currentMode: "+WebUI._dragMode+",
		// type="+WebUI._dragType);
		WebUI._dragLastX = e.clientX;
		WebUI._dragLastY = e.clientY;
		WebUI.dragResetTimer();
	},

	dragCreateCopy : function(source) {
		var dv = document.createElement('div');

		// If we drag a TR we need to encapsulate the thingy in a table/tbody to prevent trouble.
		if(source.tagName != "TR") {
			dv.innerHTML = source.innerHTML;
		} else {
			//-- This IS a tr. Create a table/TBody then add the content model
			var t = document.createElement('table');
			dv.appendChild(t);
			var b = document.createElement('tbody');
			t.appendChild(b);
			b.innerHTML = source.innerHTML;			// Copy tr inside tbody we just constructed

			//-- Find parent table's CSS class so we can copy it's style.
			var dad = WebUI.findParentOfTagName(source, 'TABLE');
			if(dad) {
				t.className= dad.className;
			}
		}

		dv.style.position = 'absolute';
		dv.style.width = $(source).width() + "px";
		dv.style.height = $(source).height() + "px";
		//console.debug("DragNode isa "+source.tagName+", "+dv.innerHTML);
		return dv;
	},

	/**
	 * Resets the dropzone timer. Called when in DRAGGING mode and the mouse
	 * moves, this resets any "open" dropzone indicators and resets the timer on
	 * which drop zone effects are done. This causes the dropzone indicator
	 * delay when moving the mouse.
	 */
	dragResetTimer : function() {
		WebUI.dragClearTimer();
		WebUI._dragTimer = setTimeout("WebUI.dragTimerFired()", 250);
	},
	dragClearTimer : function() {
		if (WebUI._dragTimer) {
			clearTimeout(WebUI._dragTimer);
			delete WebUI._dragTimer;
		}
	},

	/**
	 * Fires when in DRAGGING mode and the mouse has not moved for a while. It
	 * initiates the rendering of any drop zone indicators if the mouse is above
	 * a drop zone.
	 */
	dragTimerFired : function() {
		// console.debug("timer fired");
		var dz = WebUI.dropTargetFind(WebUI._dragLastX, WebUI._dragLastY);
		if (!dz) {
			WebUI.dropClearZone();
			return;
		}

		// -- Un-notify the previous dropzone and notify the new'un
		if (dz == WebUI._currentDropZone) {
			dz._drophandler.checkRerender(dz);
			return;
		}
		WebUI.dropClearZone();
		WebUI._currentDropZone = dz;
		dz._drophandler.hover(dz);
		// console.debug("AlterClass on "+dz._dropTarget);
	},

	findDropZoneHandler : function(type) {
		if (type == "ROW")
			return WebUI._ROW_DROPZONE_HANDLER;
		return WebUI._DEFAULT_DROPZONE_HANDLER;
	},

	dropClearZone : function() {
		if (WebUI._currentDropZone) {
			WebUI._currentDropZone._drophandler.unmark(WebUI._currentDropZone);
			delete WebUI._currentDropZone;
		}
	},

	/**
	 * Clears any node being dragged.
	 */
	dragReset : function() {
		WebUI.dragClearTimer();
		if (WebUI._dragCopy) {
			$(WebUI._dragCopy).remove();
			WebUI._dragCopy = null;
		}
		if (WebUI._dragNode) {
			$(document.body).unbind("mousemove", WebUI.dragMouseMove);
			$(document.body).unbind("mouseup", WebUI.dragMouseUp);
			WebUI._dragNode = null;
		}
		WebUI.dropClearZone();
		WebUI._dragMode = 0; // NOTDRAGGED

		if(document.detachEvent){
			document.detachEvent( "onselectstart", WebUI.preventSelection);
		}

//		if(WebUI._selectStart){
//			document.onselectstart = WebUI._selectStart;
//		}
	},

	/**
	 * Gets or recalculates the list of possible drop targets and their absolute
	 * on-screen position. This list is used to determine if the mouse is "in" a
	 * drop target. The list gets cached globally in the WebUI object; if an
	 * AJAX request is done the list gets cleared.
	 */
	dropGetList : function() {
		if (WebUI._dropList)
			return WebUI._dropList;

		// -- Reconstruct the droplist. Find all objects that possess the
		// ui-drpbl class.
		var dl = $(".ui-drpbl").get();
		WebUI._dropList = new Array();
		for ( var i = dl.length; --i >= 0;) {
			var drop = dl[i];
			var types = drop.getAttribute('uitypes');
			if (!types)
				continue;
			var def = new Object();
			def._dropTarget = drop; // Store the objects' DOM node,
			def._position = WebUI.getAbsolutePosition(drop);
			def._width = drop.clientWidth;
			def._height = drop.clientHeight;
			var tar = types.split(",");
			def._types = tar;
			def._drophandler = WebUI.findDropZoneHandler(drop
				.getAttribute('uidropmode'));
			var id = drop.getAttribute('uidropbody');
			if (id) {
				def._tbody = document.getElementById(id);
				if (!def._tbody) {
					alert('Internal error: the TBODY ID=' + id + ' cannot be located (row dropTarget)');
					continue;
				}
				WebUI.dropRemoveNonsense(def._tbody);
			}

			WebUI._dropList.push(def);
		}
		return WebUI._dropList;
	},
	dropClearList : function() {
		delete WebUI._dropList;
	},
	dropTargetFind : function(x, y) {
		var dl = WebUI.dropGetList();
		for ( var i = dl.length; --i >= 0;) {
			var d = dl[i];

			// -- Contained and of the correct type?
			if (x >= d._position.x && x < d._position.x + d._width
				&& y >= d._position.y && y < d._position.y + d._height) {
				for ( var j = d._types.length; --j >= 0;) {
					if (d._types[j] == WebUI._dragType)
						return d;
				}
			}
		}
		return null;
	},

	dropRemoveNonsense : function(body) {
		for ( var i = body.childNodes.length; --i >= 0;) {
			var n = body.childNodes[i];
			if (n.nodeName == '#text')
				body.removeChild(n);
		}
	},



	/** ***************** ScrollableTabPanel stuff. **************** */


	/***** Popup menu code *****/

	_popinCloseList: [],

	popupMenuShow: function(refid, menu) {
		WebUI.registerPopinClose(menu);
		var pos = $(refid).offset();
		var eWidth = $(refid).outerWidth();
		var mwidth = $(menu).outerWidth();
		var left = (pos.left);
		if(left + mwidth > screen.width)
			left = screen.width - mwidth - 10;
		var top = 3+pos.top;
		$(menu).css( {
			position: 'absolute',
			zIndex: 100,
			left: left+"px",
			top: top+"px"
		});

		$(menu).hide().fadeIn();
	},

	popupSubmenuShow: function(parentId, submenu) {
		$(submenu).position({my: 'left top', at: 'center top', of: parentId});
	},

	/**
	 * Register the popup. If the mouse leaves the popup window the popup needs to send a POPINCLOSE? command; this
	 * will tell DomUI server that the popin needs to go. If an item inside the popin is clicked it should mean the
	 * popin closes too; at that point we will deregister the mouse listener to prevent sending double events.
	 *
	 * @param id
	 */
	registerPopinClose: function(id) {
		WebUI._popinCloseList.push(id);
		$(id).bind("mouseleave", WebUI.popinMouseClose);
		if(WebUI._popinCloseList.length != 1)
			return;
		$(document.body).bind("keydown", WebUI.popinKeyClose);
//		$(document.body).bind("beforeclick", WebUI.popinBeforeClick);	// Called when a click is done somewhere - not needed anymore, handled from java
	},

	popinClosed: function(id) {
		for(var i = 0; i < WebUI._popinCloseList.length; i++) {
			if(id === WebUI._popinCloseList[i]) {
				//-- This one is done -> remove mouse handler.
				$(id).unbind("mousedown", WebUI.popinMouseClose);
				WebUI._popinCloseList.splice(i, 1);
				if(WebUI._popinCloseList.length == 0) {
					$(document.body).unbind("keydown", WebUI.popinKeyClose);
					$(document.body).unbind("beforeclick", WebUI.popinBeforeClick);
				}
				return;
			}
		}
	},

	popinBeforeClick: function(ee1, obj, clickevt) {
		for(var i = 0; i < WebUI._popinCloseList.length; i++) {
			var id = WebUI._popinCloseList[i];
			obj = $(obj);
			var cl = obj.closest(id);
			if(cl.size() > 0) {
				//-- This one is done -> remove mouse handler.
				$(id).unbind("mousedown", WebUI.popinMouseClose);
				WebUI._popinCloseList.splice(i, 1);
				if(WebUI._popinCloseList.length == 0) {
					$(document.body).unbind("keydown", WebUI.popinKeyClose);
					$(document.body).unbind("beforeclick", WebUI.popinBeforeClick);
				}
				return;
			}
		}
	},

	popinMouseClose: function() {
		if(WebUI.isUIBlocked())							// We will get a LEAVE if the UI blocks during menu code... Ignore it
			return;

		try {
			for(var i = 0; i < WebUI._popinCloseList.length; i++) {
				var id = WebUI._popinCloseList[i];
				var el = $(id);
				if(el) {
					el.unbind("mousedown", WebUI.popinMouseClose);
					WebUI.scall(id.substring(1), "POPINCLOSE?", {});
				}
			}
		} finally {
			WebUI._popinCloseList = [];
//			$(document.body).unbind("mousedown", WebUI.popinMouseClose);
			$(document.body).unbind("keydown", WebUI.popinKeyClose);
			$(document.body).unbind("beforeclick", WebUI.popinBeforeClick);
		}
	},
	popinKeyClose: function(evt) {
		if(! evt)
			evt = window.event;
		var kk = WebUI.normalizeKey(evt);
		if(kk == 27 || kk == 27000) {
			// Prevent ESC from cancelling the AJAX call in Firefox!!
			evt.preventDefault();
			evt.cancelBubble = true;
			if(evt.stopPropagation)
				evt.stopPropagation();
			WebUI.popinMouseClose();
		}
	},


	// CK editor support, map of key (id of editor) value (pair of [editor instance, assigned resize function])
	_ckEditorMap : {},

	/**
	 * Register ckeditor for extra handling that is set in CKeditor_OnComplete.
	 *
	 * @param id
	 * @param ckeInstance
	 */
	registerCkEditorId : function(id, ckeInstance) {
		WebUI._ckEditorMap[id] = [ckeInstance, null];
	},

	/**
	 * Unregister ckeditor and removes handlings bound to it.
	 *
	 * @param id
	 */
	unregisterCkEditorId : function(id) {
		try {
			var editorBindings = WebUI._ckEditorMap[id];
			WebUI._ckEditorMap[id] = null;
			if (editorBindings && editorBindings[1]){
				$(window).unbind('resize', editorBindings[1]);
			}
		} catch (ex) {
			WebUI.log('error in unregisterCkEditorId: ' + ex);
		}
	},

	/**
	 * Piece of support needed for CK editor to properly fix its size, using _OnComplete handler.
	 *
	 * @param id
	 */
	CKeditor_OnComplete : function(id) {
		WebUI.doCustomUpdates();
		var elem = document.getElementById(id);
		var parentDiv = elem.parentNode;
		var editor = WebUI._ckEditorMap[id][0];
		var resizeFunction = function(ev) {
			try{
				editor.resize($(parentDiv).width() - 2, $(parentDiv).height());
			}catch (ex){
				WebUI.log('error in CKeditor_OnComplete#resizeFunction: ' + ex);
			}
		};
		WebUI._ckEditorMap[id] = [editor, resizeFunction];
		$(window).bind('resize', resizeFunction);
		$(window).trigger('resize');
	},

	// connects input to usually hidden list select and provides autocomplete
	// feature inside input. Down arrow does show and focus select list.
	initAutocomplete : function(inputId, selectId) {
		var input = document.getElementById(inputId);
		var select = document.getElementById(selectId);
		$(input).keyup(function(event) {
			WebUI.autocomplete(event, inputId, selectId);
		});
		$(select).keypress(function(event) {
			//esc hides select and prevents fireing of click and blur handlers that are temporary disconnected while focus moves back to input
			var keyCode = WebUI.normalizeKey(event);
			if (keyCode == 27 || keyCode == 27000) {
				var oldVal = input.value;
				var selectOnClick = select.click;
				var selectOnBlur = select.blur;
				select.click = null;
				select.blur = null;
				select.style.display = 'none';
				input.focus();
				input.value = oldVal;
				select.click = selectOnClick;
				select.blur = selectOnBlur;
			}
		});
	},

	// does autocomplete part of logic
	autocomplete : function (event, inputId, selectId) {
		var select = document.getElementById(selectId);
		var cursorKeys = "8;46;37;38;39;40;33;34;35;36;45;";
		if (cursorKeys.indexOf(event.keyCode + ";") == -1) {
			var input = document.getElementById(inputId);
			var found = false;
			var foundAtIndex = -1;
			for (var i = 0; i < select.options.length; i++){
				if ((found = select.options[i].text.toUpperCase().indexOf(input.value.toUpperCase()) == 0)){
					foundAtIndex = i;
					break;
				}
			}
			select.selectedIndex = foundAtIndex;

			var oldValue = input.value;
			var newValue = found ? select.options[foundAtIndex].text : oldValue;
			if (newValue != oldValue) {
				if (typeof input.selectionStart != "undefined") {
					//normal browsers
					input.value = newValue;
					input.selectionStart = oldValue.length;
					input.selectionEnd =  newValue.length;
					input.focus();
				}
				if (document.selection && document.selection.createRange) {
					//IE9
					input.value = newValue;
					input.focus();
					input.select();
					var range = document.selection.createRange();
					range.collapse(true);
					range.moveStart("character", oldValue.length);
					range.moveEnd("character", newValue.length);
					range.select();
				}else if (input.createTextRange) {
					//IE8-
					input.value = newValue;
					var rNew = input.createTextRange();
					rNew.moveStart('character', oldValue.length);
					rNew.select();
				}
			}
		}else if (event.keyCode == 40){
			select.style.display = 'inline';
			select.focus();
		}
	},

	//alignment methods
	//sticks top of element with nodeId to bottom of element with alignToId, with extra offsetY.
	alignTopToBottom : function (nodeId, alignToId, offsetY, doCallback){
		var alignNode = $('#' + alignToId);
		var node = $('#' + nodeId);
		var myTopPos;
		if (node.css('position') == 'fixed'){
			myTopPos = $(alignNode).offset().top - $(document).scrollTop() + offsetY + $(alignNode).outerHeight(true);
		}else{
			myTopPos = $(alignNode).position().top + offsetY + $(alignNode).outerHeight(true);
		}
		$(node).css('top', myTopPos);
		if (doCallback){
			WebUI.notifySizePositionChangedOnId(nodeId);
		}
	},

	//align top of element with nodeId to top of element with alignToId, with extra offsetY
	alignToTop : function (nodeId, alignToId, offsetY, doCallback){
		var alignNode = $('#' + alignToId);
		var node = $('#' + nodeId);
		var myTopPos;
		if (node.css('position') == 'fixed'){
			myTopPos = $(alignNode).offset().top - $(document).scrollTop() + offsetY;
		}else{
			myTopPos = $(alignNode).position().top + offsetY;
		}
		var nodeHeight = $(node).outerHeight(true);
		if (myTopPos + nodeHeight > $(window).height()){
			myTopPos = $(window).height() - nodeHeight;
		}
		$(node).css('top', myTopPos);
		if (doCallback){
			WebUI.notifySizePositionChangedOnId(nodeId);
		}
	},

	//align left edge of element with nodeId to left edge of element with alignToId, with extra offsetX
	alignToLeft : function (nodeId, alignToId, offsetX, doCallback){
		var node = $('#' + nodeId);
		var alignNode = $('#' + alignToId);
		var myLeftPos;
		if (node.css('position') == 'fixed'){
			myLeftPos = $(alignNode).offset().left - $(document).scrollLeft() + offsetX;
		}else{
			myLeftPos = $(alignNode).position().left + offsetX;
		}
		var nodeWidth = $(node).outerWidth(true);
		if (myLeftPos + nodeWidth > $(window).width()){
			myLeftPos = $(window).width() - nodeWidth;
			if (myLeftPos < 1){
				myLeftPos = 1;
			}
		}
		$(node).css('left', myLeftPos);
		if (doCallback){
			WebUI.notifySizePositionChangedOnId(nodeId);
		}
	},

	//align right edge of element with nodeId to right edge of element with alignToId, with extra offsetX
	alignToRight : function (nodeId, alignToId, offsetX, doCallback){
		var node = $('#' + nodeId);
		var alignNode = $('#' + alignToId);
		var myLeftPos;
		if (node.css('position') == 'fixed'){
			myLeftPos = $(alignNode).offset().left - $(document).scrollLeft() + offsetX - $(node).outerWidth(true) + $(alignNode).outerWidth(true) - 3;
		}else{
			myLeftPos = $(alignNode).position().left + offsetX - $(node).outerWidth(true) + $(alignNode).outerWidth(true) - 3;
		}
		if (myLeftPos < 1){
			myLeftPos = 1;
		}
		$(node).css('left', myLeftPos);
		if (doCallback){
			WebUI.notifySizePositionChangedOnId(nodeId);
		}
	},

	//align horizontaly middle of element with nodeId to middle of element with alignToId, with extra offsetX
	alignToMiddle : function (nodeId, alignToId, offsetX, doCallback){
		var node = $('#' + nodeId);
		var alignNode = $('#' + alignToId);
		var myLeftPos;
		if (node.css('position') == 'fixed'){
			myLeftPos = $(alignNode).offset().left - $(document).scrollLeft() + ($(alignNode).outerWidth(true) / 2) - ($(node).outerWidth(true) / 2);
		}else{
			myLeftPos = $(alignNode).position().left + ($(alignNode).outerWidth(true) / 2) - ($(node).outerWidth(true) / 2);
		}
		if (myLeftPos < 1){
			myLeftPos = 1;
		}
		$(node).css('left', myLeftPos);
		if (doCallback){
			WebUI.notifySizePositionChangedOnId(nodeId);
		}
	}
});

WebUI._DEFAULT_DROPZONE_HANDLER = {
	checkRerender : function(dz) {
	},
	hover : function(dz) {
		$(dz._dropTarget).addClass("ui-drp-hover");
	},
	unmark : function(dz) {
		if (dz)
			$(dz._dropTarget).removeClass("ui-drp-hover");
	},

	drop : function(dz) {
		this.unmark(dz);
		WebUI.scall(dz._dropTarget.id, "WEBUIDROP", {
			_dragid :WebUI._dragNode.id,
			_index :0
		});
		WebUI.dragReset();
	}
};

/**
 * This handles ROW mode drops. It locates the nearest row in the TBody for this
 * dropTarget and decides to put the thingy BEFORE or AFTER that row. The
 * boundary there then gets highlighted.
 */
WebUI._ROW_DROPZONE_HANDLER = {
	locateBest : function(dz) {
		var tbody = dz._tbody;
		if (!tbody)
			throw "No TBody!";

		// -- Use the current mouseish Y position to distinguish between rows.
		var mousePos = WebUI._dragLastY;
		var mouseX = WebUI._dragLastX;
		//console.debug("Starting position det: drag Y = "+mousePos);
		var gravity = 0; // Prefer upward gravity
		var lastrow = null;
		var rowindex = 0;
		var position = { top: 0, index: 0};
		for ( var i = 0; i < tbody.childNodes.length; i++) {
			var tr = tbody.childNodes[i];
			if (tr.nodeName != 'TR')
				continue;
			lastrow = tr;
			var off = $(tr).offset();
			var prevPosition = position;
			position = { top: off.top, index: i };
			if (position) {
				//			console.debug('mouse:' +mousePos+','+mouseX+' row: prevPosition.top='+prevPosition.top+", position.top="+position.top+", index="+position.index);

				// -- Is the mouse IN the Y range for this row?
				if (mousePos >= prevPosition.top && mousePos < position.top) {
					// -- Cursor is WITHIN this node. Is it near the TOP or near the
					// BOTTOM?
					gravity = 0;
					if(prevPosition.top + position.top != 0){
						var hy = (prevPosition.top + position.top) / 2;
						gravity = mousePos < hy ? 0 : 1;
					}
					//				console.debug('ACCEPTED top='+prevPosition.top+', bottom='+position.top+', hy='+hy+', rowindex='+(rowindex-1));
					//				console.debug('index='+prevPosition.index+', gravety='+gravity);

					var colIndex = this.getColIndex(tr, mouseX);
					return {
						index :rowindex-1,
						iindex : prevPosition.index,
						gravity :gravity,
						row :tr,
						colIndex : colIndex
					};
				}

				// -- Is the thing between this row and the PREVIOUS one?
				//			if (mousePos < position.top) {
				//				// -- Use this row with gravity 0 (should insert BEFORE this row).
				//				//MVE
				//				console.debug('ACCEPTED BEFORE node by='+prevPosition.top+', ey='+position.top+', rowindex='+rowindex-1);
				//				return {
				//					index :rowindex,
				//					iindex :position.index,
				//					gravity :0,
				//					row :tr
				//				};
				//			}
				//console.debug('REFUSED by='+prevPosition.top+", ey="+position.top+", rowindex="+rowindex);
			} else {
				//			console.debug("row: no location.");
			}
			rowindex++;
		}
		//console.debug("ACCEPTED last one");

		// -- If we're here we must insert at the last location
		var colIndex = this.getColIndex(lastrow, mouseX);
		return {
			index :rowindex,
			iindex :position.index,
			gravity :1,
			row :lastrow,
			colIndex : colIndex
		};
	},

	getColIndex : function(tr, mouseX) {
		//determine the collumn
		var left = 0;
		var right = 0;
		var j;
		for ( j = 0; j < tr.childNodes.length; j++) {
			var td = tr.childNodes[j];
			if (td.nodeName != 'TD')
				continue;
			left = right;
			right = $(td).offset().left;
			if(mouseX >= left && mouseX < right ){
				//because only the left position can be asked, the check is done for the previous collumn
				return j-1;
			}

		}
		//TODO MVE should return maxColumn
		return 2;

	},

	checkRerender : function(dz) {
		var b = this.locateBest(dz);
		// console.debug("checkRerender: "+b.iindex+", "+b.index+", g="+b.gravity);
		if (b.iindex == WebUI._dropRowIndex)
			return;

		this.unmark(dz);
		this.renderTween(dz, b);
	},

	renderTween : function(dz, b) {
		var body = dz._tbody;

		var colCount = 0;
		if(dz._tbody.rows.length > 0){
			var temp = dz._tbody.rows[0].cells;
			$(temp).each(function() {
				colCount += $(this).attr('colspan') ? parseInt($(this).attr('colspan')) : 1;
			});
		}


		// -- To mark, we insert a ROW at the insert location and visualize that
		var tr = document.createElement('tr');
		//b.colIndex should define the correct collumn
		var colIndex = b.colIndex;
		for(var i = 0; i<colCount;i++ ){
			this.appendPlaceHolderCell(tr, colIndex == i);
		}
		if (b.iindex >= body.childNodes.length)
			body.appendChild(tr);
		else
			body.insertBefore(tr, body.childNodes[b.iindex]);
		WebUI._dropRow = tr;
		WebUI._dropRowIndex = b.iindex;
	},

	appendPlaceHolderCell : function(tr, appendPlaceholder) {
		var td = document.createElement('td');
		if(appendPlaceholder){
			td.appendChild(document.createTextNode(WebUI._T.dndInsertHere));
			td.className = 'ui-drp-ins';
		}
		tr.appendChild(td);

	},

	hover : function(dz) {
		var b = this.locateBest(dz);
		//	console.debug("hover: "+b.iindex+", "+b.index+", g="+b.gravity + ", col=" +b.colIndex);
		this.renderTween(dz, b);
	},

	unmark : function(dz) {
		if (WebUI._dropRow) {
			$(WebUI._dropRow).remove();
			delete WebUI._dropRow;
			delete WebUI._dropRowIndex;
		}
	},

	drop : function(dz) {
		this.unmark(dz);
		var b = this.locateBest(dz);
		WebUI.scall(dz._dropTarget.id, "WEBUIDROP", {
			_dragid :WebUI._dragNode.id,
			_index :(b.index+b.gravity),
			_colIndex :b.colIndex
		});
		WebUI.dragReset();
	}
};

/**
 * Make a structure a color button.
 */
WebUI.colorPickerButton = function(btnid, inid, value,onchange) {
	$(btnid).ColorPicker({
		color: '#'+value,
		onShow: function (colpkr) {
			$(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			$(colpkr).fadeOut(500);
			return false;
		},
		onChange: function (hsb, hex, rgb) {
			$(btnid+' div').css('backgroundColor', '#' + hex);
			$(inid).val(hex);
			if(onchange)
				WebUI.colorPickerOnchange(btnid, hex);
		}
	});
};

WebUI.colorPickerInput = function(inid, divid, value, onchange) {
	$(inid).ColorPicker({
		color: '#'+value,
		flat: false,
		onShow: function (colpkr) {
			$(colpkr).fadeIn(500);
			return false;
		},
		onHide: function (colpkr) {
			$(colpkr).fadeOut(500);
			return false;
		},
		onBeforeShow: function() {
			$(this).ColorPickerSetColor(this.value);
		},
		onChange: function (hsb, hex, rgb) {
			$(divid).css('backgroundColor', '#' + hex);
			$(inid).val(hex);
			if(onchange)
				WebUI.colorPickerOnchange(btnid, hex);
		}
	});
};
WebUI.colorPickerDisable = function(id) {
	$(id).die();
};

WebUI.colorPickerOnchange= function(id, last) {
	if(WebUI._colorLast == last && WebUI._colorLastID == id)
		return;

	if(WebUI._colorTimer) {
		window.clearTimeout(WebUI._colorTimer);
		window._colorTimer = undefined;
	}
	WebUI._colorLastID = id;
	WebUI._colorTimer = window.setTimeout("WebUI.colorPickerChangeEvent('" + id + "')", 500);
};

WebUI.colorPickerChangeEvent = function(id) {
	window.clearTimeout(WebUI._colorTimer);
	window._colorTimer = undefined;
	WebUI.valuechanged('eh', id);
};

var DomUI = WebUI;

WebUI._customUpdatesContributors = $.Callbacks("unique");

WebUI._customUpdatesContributorsTimerID = null;


WebUI.floatingDivResize = function(ev, ui) {
	$(ui.helper.get(0)).css('position', 'fixed');
	$('[stretch=true]').doStretch();
	$('.ui-dt, .ui-fixovfl').fixOverflow();
};

WebUI.onWindowResize = function() {
	WebUI.doCustomUpdates();
};

WebUI.flare = function(id) {
	$('#'+id).fadeIn('fast', function() {
		$('#'+id).delay(500).fadeOut(1000, function() {
			$('#'+id).remove();
		});
	});
};

WebUI.flareStay = function(id) {
	$('#'+id).fadeIn('fast', function() {
		$('body,html').bind('mousemove.' + id, function(e){
			$('body,html').unbind('mousemove.' + id);
			$('#'+id).delay(500).fadeOut(1000, function() {
				$('#'+id).remove();
			});
		});
	});
};

WebUI.flareStayCustom = function(id, delay, fadeOut) {
	$('#'+id).fadeIn('fast', function() {
		$('body,html').bind('mousemove.' + id, function(e){
			$('body,html').unbind('mousemove.' + id);
			$('#'+id).delay(delay).fadeOut(fadeOut, function() {
				$('#'+id).remove();
			});
		});
	});
};

/** Bulk upload code using swfupload */
WebUI.bulkUpload = function(id, buttonId, url) {
	var ctl = $('#'+id);
	ctl.swfupload({
		upload_url: url,
		flash_url: DomUIappURL+"$js/swfupload.swf",
		file_types: '*.*',
		file_upload_limit: 1000,
		file_queue_limit: 0,
		file_size_limit: "100 MB",
		button_width: 120,
		button_height: 24,
		button_placeholder_id: buttonId,
		button_window_mode: SWFUpload.WINDOW_MODE.TRANSPARENT,
		button_cursor: SWFUpload.CURSOR.HAND
	});
	var target = $("#"+id+" .ui-bupl-queue");

	ctl.bind('fileQueued', function(event, file) {
		var uf = new WebUI.UploadFile(file, target, function() {
			$.swfupload.getInstance(ctl).cancelUpload(file.id);
		});
	});
	ctl.bind('uploadStart', function(event, file) {
		var uf = new WebUI.UploadFile(file, target);
		uf.uploadStarted();
	});
	ctl.bind('uploadProgress', function(event, file, bytesdone, bytestotal) {
		var uf = new WebUI.UploadFile(file, target);
		var pct = bytesdone * 100 / bytestotal;
		uf.setProgress(pct);
	});
	ctl.bind('uploadError', function(event, file, code, msg) {
		var uf = new WebUI.UploadFile(file, target);
		uf.uploadError(msg);
	});
	ctl.bind('uploadSuccess', function(event, file, code, msg) {
		var uf = new WebUI.UploadFile(file, target);
		uf.uploadComplete();

		//-- Send a DomUI command so the UI can handle updates.
		WebUI.scall(id, "uploadDone", {});
	});
	ctl.bind('queueComplete', function(event, numUploaded) {
		//-- Send a DomUI command for queue complete.
		WebUI.scall(id, "queueComplete", {});
	});
//	ctl.bind('uploadComplete', function(event, file) {
//		var uf = new WebUI.UploadFile(file, target);
//	});
	ctl.bind('fileDialogComplete', function(nfiles) {
		if(0 == nfiles) {
			return;
		}

		//-- Autostart upload on dialog completion.
		ctl.swfupload('startUpload');

		//-- Send a DomUI command for queue start.
		WebUI.scall(id, "queueStart", {});
	});
	ctl.bind('fileQueueError', function(event, file, errorCode, message) {
		try {
			if(errorCode === SWFUpload.QUEUE_ERROR.QUEUE_LIMIT_EXCEEDED) {
				alert(WebUI._T.buplTooMany);
//				alert("You have attempted to queue too many files.\n" + (message === 0 ? "You have reached the upload limit." : "You may select " + (message > 1 ? "up to " + message + " files." : "one file.")));
				return;
			}
			var uf = new WebUI.UploadFile(file, target);
			switch (errorCode) {
				case SWFUpload.QUEUE_ERROR.FILE_EXCEEDS_SIZE_LIMIT:
					uf.uploadError(WebUI._T.buplTooBig);
					break;
				case SWFUpload.QUEUE_ERROR.ZERO_BYTE_FILE:
					uf.uploadError(WebUI._T.buplEmptyFile);
					break;
				case SWFUpload.QUEUE_ERROR.INVALID_FILETYPE:
					uf.uploadError(WebUI._T.buplInvalidType);
					break;
				default:
					if(file !== null) {
						uf.uploadError(WebUI._T.buplUnknownError);
					}
					break;
			}
		} catch (ex) {
			alert(ex);
		}
	});

//	//-- TEST
//	var file = {
//			id: 'jal',
//			name: 'upload.jpg'
//	};
//	var uf = new WebUI.UploadFile(file, target, function() {
//		$.swfupload.getInstance(ctl).cancelUpload(file.id);
//	});
//
//	setTimeout(function() {
//		uf.uploadStarted();
//
//	}, 2000);
//
//	setTimeout(function() {
//		uf.setProgress(10);
//
//	}, 3000);
//
//	setTimeout(function() {
//		uf.setProgress(20);
//
//	}, 4000);
//
////	setTimeout(function() {
////		uf.uploadError("Server IO error");
////
////	}, 6000);
//
//	setTimeout(function() {
//		uf.uploadComplete();
//	}, 6000);
};

/**
 * The uploadFile object handles all progress handling for a swf file.
 */
WebUI.UploadFile = function(file, target, cancelFn) {
	this._id = file.id;

	//-- connect to pre-existing UI
	this._ui = $('#'+file.id);
	if(this._ui.length == 0) {
		//-- Create the UI.
		target.append("<div id='"+this._id+"' class='ui-bupl-file'><div class='ui-bupl-inner ui-bupl-pending'><a href='#' class='ui-bupl-cancl'> </a><div class='ui-bupl-name'>"+file.name+"</div><div class='ui-bupl-stat'>"+DomUI._T.buplPending+"</div><div class='ui-bupl-perc'></div></div></div>");
		this._ui = $('#'+file.id);
		if(cancelFn) {
			var me = this;
			$(".ui-bupl-cancl", this._ui).bind("click", function() {
				$(".ui-bupl-stat", this._ui).html(WebUI._T.buplCancelled);
				me.suicide();
				cancelFn();
			});
		}
	}
};
WebUI.UploadFile.prototype.uploadStarted = function() {
	$(".ui-bupl-stat", this._ui).html(WebUI._T.buplRunning);
	$(".ui-bupl-inner", this._ui).removeClass("ui-bupl-pending").addClass("ui-bupl-running");
};
WebUI.UploadFile.prototype.setProgress = function(pct) {
	$(".ui-bupl-perc", this._ui).width(pct+"%");
};
WebUI.UploadFile.prototype.uploadError = function(message) {
	$(".ui-bupl-stat", this._ui).html(WebUI._T.buplError+": "+message);
	$(".ui-bupl-inner", this._ui).removeClass("ui-bupl-pending").removeClass("ui-bupl-running").addClass("ui-bupl-error");
	$(".ui-bupl-cancl", this._ui).remove();
	this.setProgress(0);
	this.suicide();
};
WebUI.UploadFile.prototype.uploadComplete = function() {
	$(".ui-bupl-stat", this._ui).html(WebUI._T.buplComplete);
	$(".ui-bupl-inner", this._ui).removeClass("ui-bupl-pending").removeClass("ui-bupl-running").removeClass("ui-bupl-error").addClass("ui-bupl-complete");
	this.setProgress(100);
	$(".ui-bupl-cancl", this._ui).remove();
	this.suicide();
};
WebUI.UploadFile.prototype.suicide = function() {
	this._ui.delay(8000).fadeOut(500);
};


$(document).ready(WebUI.onDocumentReady);
$(window).resize(WebUI.onWindowResize);
$(document).ajaxComplete( function() {
	WebUI.handleCalendarChanges();
	WebUI.doCustomUpdates();
});

$(document).keydown(function(e){
	addPaggerAccessKeys(e);
});

function addPaggerAccessKeys(e) {
	var KEY = {
		HOME		: 36,
		END			: 35,
		PAGE_UP		: 33,
		PAGE_DOWN	: 34
	};
	if ($('div.ui-dp-btns').size() > 0) {
		if (e.altKey){
			if(e.keyCode == KEY.HOME) {
				$("div.ui-dp-btns > a:nth-child(1)").click();
			} else if (e.keyCode == KEY.PAGE_UP) {
				$("div.ui-dp-btns > a:nth-child(2)").click();
			} else if (e.keyCode == KEY.PAGE_DOWN) {
				$("div.ui-dp-btns > a:nth-child(3)").click();
			} else if (e.keyCode == KEY.END) {
				$("div.ui-dp-btns > a:nth-child(4)").click();
			}
		}
	}
}

//piece of support needed for FCK editor to properly fix heights in IE8+
function FCKeditor_OnComplete(editorInstance){
	if (WebUI.isIE8orNewer()){
		for (var i = 0; i < WebUI._fckEditorIDs.length; i++) {
			var fckId = WebUI._fckEditorIDs[i];
			var fckIFrame = document.getElementById(fckId + '___Frame');
			if (fckIFrame){
				$(fckIFrame.contentWindow.window).bind('resize', function()
				{
					FCKeditor_fixLayout(fckIFrame, fckId);
				});
				$(fckIFrame.contentWindow.window).trigger('resize');
			};
		};
	};
}

WebUI.initScrollableTableOld = function(id) {
	$('#'+id+" table").fixedHeaderTable({});
	var sbody = $('#'+id+" .fht-tbody");
	sbody.scroll(function() {
		var bh = $(sbody).height();
		var st = $(sbody).scrollTop()
		var tbl = $('#'+id+" .fht-table tbody");
		var th = tbl.height();
		var left = tbl.height() - bh - st;
		//$.dbg("scrolling: bodyheight="+bh+" scrolltop="+st+" tableheight="+th+" left="+left);

		if(left > 100) {
			//$.dbg("Scrolling: area left="+left);
			return;
		}

		var lastRec = sbody.find("tr[lastRow]");
		if(lastRec.length != 0) {
			//$.dbg("scrolling: lastrec found");
			return;
		}
		WebUI.scall(id, "LOADMORE", {});
	});

};

WebUI.scrollableTableReset = function(id, tblid) {
	var tbl = $('#'+tblid);
	var container = $('#'+id);
	tbl.floatThead('reflow');
	WebUI.doCustomUpdates();

	$.dbg('recreate');

	//tbl.floatThead('destroy');
	//tbl.floatThead({
	//	scrollContainer: function() {
	//		return container;
	//	}
	//});

	container.scrollTop(0);
};

WebUI.initScrollableTable = function(id, tblid) {
	var container = $('#'+id);
	var tbl = $('#'+tblid);
	WebUI.doCustomUpdates();

	tbl.floatThead({
		scrollContainer: function() {
			return container;
		},
		getSizingRow: function($table){ // this is only called when using IE, we need any row without colspan, see http://mkoryak.github.io/floatThead/examples/row-groups/
			var rows = $table.find('tbody tr:visible').get();
			for (var i = 0; i < rows.length; i++){
				var cells = $(rows[i]).find('td');
				var isInvalidRow = false;
				for (var i = 0; i < cells.get().length; i++){
					if ($(cells[i]).attr('colspan') > 1){
						isInvalidRow = true;
					}
				}
				if (!isInvalidRow){
					return cells;
				}
			}
			if (rows.length > 0) {
				return $(rows[0]).find('td'); //as fallback we just return first row cells
			}else{
				return null; //or nothing -> but this should not be possible since getSizingRow is called only on table with rows
			}
		}
	});
	container.scroll(function() {
		var bh = $(container).height();
		var st = $(container).scrollTop()
		var tbl = $('#'+id+" tbody");
		var th = tbl.height();
		var left = tbl.height() - bh - st;
		$.dbg("scrolling: bodyheight="+bh+" scrolltop="+st+" tableheight="+th+" left="+left);

		if(left > 100) {
			//$.dbg("Scrolling: area left="+left);
			return;
		}

		var lastRec = tbl.find("tr[lastRow]");
		if(lastRec.length != 0) {
			//$.dbg("scrolling: lastrec found");
			return;
		}
		WebUI.scall(id, "LOADMORE", {

		});
	});

};




WebUI.notifyPage = function(command) {
	var bodyId = '_1';
	var pageBody = document.getElementById(bodyId);
	//check for exsistence, since it is delayed action component can be removed when action is executed.
	if (pageBody){
		var fields = new Object();
		fields.webuia = "notifyPage";
		fields[bodyId + "_command"] = command;
		WebUI.scall(bodyId, "notifyPage", fields);
	}
};

WebUI.closeOnClick = function(id) {
	this._id = id;
	var clickHandler = this._clickHandler = $.proxy(this.closeMenu, this);
	$(document).click(clickHandler);
	var keyUpHandler = this._keyUpHandler = $.proxy(this.buttonHandler, this);
	$(document).keyup(keyUpHandler);
	$('#' + id).data('inst', this);
};

$.extend(WebUI.closeOnClick.prototype, {
	closeMenu: function() {
		this.unbind();
		WebUI.scall(this._id, "CLOSEMENU?", {});
	},

	unbind: function() {
		$(document).unbind("click", this._clickHandler);
		$(document).unbind("keyup", this._keyUpHandler);
	},

	markClosed: function(id) {
		var inst = $('#' + id).data('inst');
		if(inst) {
			inst.unbind();
		}
	},

	isInputTagEvent: function(event) {
		var src = event.srcElement;
		if(src) {
			var tn = src.tagName.toUpperCase();
			if(tn === 'INPUT' || tn == 'SELECT' || tn == "TEXTAREA")
				return true;
		}
		return false;
	},

	buttonHandler: function(event) {
		if (this.isInputTagEvent(event))
			return;

		if (event.which == 27) {				// escape
			this.closeMenu();
		}
	}
});

DbPerformance = new Object();
DbPerformance.post = function(id,sessionid) {
	$(document).ready(function() {
		setTimeout(function() {
			$.get(DomUIappURL + "nl.itris.vp.parts.DbPerf.part?requestid=" + sessionid, function(data) {
				//-- Insert the div as the last in the body
				$('#' + id).html(data);
				$(".vp-lspf").draggable({ghosting: false, zIndex: 100, handle: '.vp-lspf-ttl'});
				$(".vp-lspf-close").click(function() {
					$(".vp-lspf").hide();
				});

			});
		}, 500);

	})
};
