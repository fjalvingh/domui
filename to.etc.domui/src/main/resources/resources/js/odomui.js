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
