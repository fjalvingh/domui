/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.d.ts" />
//import WebUI from "domui.webui.util";

namespace WebUIStatic {
	let _selectStart = undefined;

	let _popinCloseList = [];

	function oddCharAndClickCallback(nodeId, clickId): void {
		oddChar(document.getElementById(nodeId));
		document.getElementById(clickId).click();
	}

	function oddChar(obj) {
		WebUI.toClip(obj.innerHTML);
	}

	function popupMenuShow(refid, menu): void {
		registerPopinClose(menu);
		var pos = $(refid).offset();
		var eWidth = $(refid).outerWidth();
		var mwidth = $(menu).outerWidth();
		var left = (pos.left);
		if(left + mwidth > screen.width)
			left = screen.width - mwidth - 10;
		var top = 3 + pos.top;
		$(menu).css({
			position: 'absolute',
			zIndex: 100,
			left: left + "px",
			top: top + "px"
		});

		$(menu).hide().fadeIn();
	}

	function popupSubmenuShow(parentId, submenu) {
		$(submenu).position({my: 'left top', at: 'center top', of: parentId});
	}

	/**
	 * Register the popup. If the mouse leaves the popup window the popup needs to send a POPINCLOSE? command; this
	 * will tell DomUI server that the popin needs to go. If an item inside the popin is clicked it should mean the
	 * popin closes too; at that point we will deregister the mouse listener to prevent sending double events.
	 *
	 * @param id
	 */
	function registerPopinClose(id): void {
		_popinCloseList.push(id);
		$(id).bind("mouseleave", popinMouseClose);
		if(_popinCloseList.length != 1)
			return;
		$(document.body).bind("keydown", popinKeyClose);
//		$(document.body).bind("beforeclick", WebUI.popinBeforeClick);	// Called when a click is done somewhere - not needed anymore, handled from java
	}

	function popinClosed(id): void {
		for(var i = 0; i < _popinCloseList.length; i++) {
			if(id === _popinCloseList[i]) {
				//-- This one is done -> remove mouse handler.
				$(id).unbind("mousedown", popinMouseClose);
				_popinCloseList.splice(i, 1);
				if(_popinCloseList.length == 0) {
					$(document.body).unbind("keydown", popinKeyClose);
					$(document.body).unbind("beforeclick", popinBeforeClick);
				}
				return;
			}
		}
	}

	function popinBeforeClick(ee1, obj, clickevt): void {
		for(var i = 0; i < _popinCloseList.length; i++) {
			var id = _popinCloseList[i];
			obj = $(obj);
			var cl = obj.closest(id);
			if(cl.size() > 0) {
				//-- This one is done -> remove mouse handler.
				$(id).unbind("mousedown", popinMouseClose);
				_popinCloseList.splice(i, 1);
				if(_popinCloseList.length == 0) {
					$(document.body).unbind("keydown", popinKeyClose);
					$(document.body).unbind("beforeclick", popinBeforeClick);
				}
				return;
			}
		}
	}

	function popinMouseClose(): void {
		if(WebUI.isUIBlocked())							// We will get a LEAVE if the UI blocks during menu code... Ignore it
			return;

		try {
			for(var i = 0; i < _popinCloseList.length; i++) {
				var id = _popinCloseList[i];
				var el = $(id);
				if(el) {
					el.unbind("mousedown", popinMouseClose);
					WebUI.scall(id.substring(1), "POPINCLOSE?", {});
				}
			}
		} finally {
			_popinCloseList = [];
//			$(document.body).unbind("mousedown", WebUI.popinMouseClose);
			$(document.body).unbind("keydown", popinKeyClose);
			$(document.body).unbind("beforeclick", popinBeforeClick);
		}
	}

	function popinKeyClose(evt): void {
		if(!evt)
			evt = window.event;
		var kk = WebUI.normalizeKey(evt);
		if(kk == 27 || kk == 27000) {
			// Prevent ESC from cancelling the AJAX call in Firefox!!
			evt.preventDefault();
			evt.cancelBubble = true;
			if(evt.stopPropagation)
				evt.stopPropagation();
			popinMouseClose();
		}
	}


	// CK editor support, map of key (id of editor) value (pair of [editor instance, assigned resize function])
	let _ckEditorMap = {};

	/**
	 * Register ckeditor for extra handling that is set in CKeditor_OnComplete.
	 *
	 * @param id
	 * @param ckeInstance
	 */
	function registerCkEditorId(id, ckeInstance): void {
		_ckEditorMap[id] = [ckeInstance, null];
	}

	/**
	 * Unregister ckeditor and removes handlings bound to it.
	 *
	 * @param id
	 */
	function unregisterCkEditorId(id): void {
		try {
			var editorBindings = _ckEditorMap[id];
			_ckEditorMap[id] = null;
			if(editorBindings && editorBindings[1]) {
				$(window).unbind('resize', editorBindings[1]);
			}
		} catch(ex) {
			WebUI.log('error in unregisterCkEditorId: ' + ex);
		}
	}

	/**
	 * Piece of support needed for CK editor to properly fix its size, using _OnComplete handler.
	 *
	 * @param id
	 */
	function CKeditor_OnComplete(id): void {
		WebUI.doCustomUpdates();
		var elem = document.getElementById(id);
		var parentDiv = elem.parentNode;
		var editor = _ckEditorMap[id][0];
		var resizeFunction = function(ev) {
			try {
				editor.resize($(parentDiv).width() - 2, $(parentDiv).height());
			} catch(ex) {
				WebUI.log('error in CKeditor_OnComplete#resizeFunction: ' + ex);
			}
		};
		_ckEditorMap[id] = [editor, resizeFunction];
		$(window).bind('resize', resizeFunction);
		$(window).trigger('resize');
	}

	// connects input to usually hidden list select and provides autocomplete
	// feature inside input. Down arrow does show and focus select list.
	function initAutocomplete(inputId, selectId): void {
		var input = document.getElementById(inputId) as HTMLInputElement;
		var select = document.getElementById(selectId);
		$(input).keyup(function(event) {
			autocomplete(event, inputId, selectId);
		});
		$(select).keypress(function(event) {
			//esc hides select and prevents firing of click and blur handlers that are temporary disconnected while focus moves back to input
			var keyCode = WebUI.normalizeKey(event);
			if(keyCode == 27 || keyCode == 27000) {
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
	}

	// does autocomplete part of logic
	function autocomplete(event, inputId, selectId): void {
		var select = document.getElementById(selectId) as HTMLSelectElement;
		var cursorKeys = "8;46;37;38;39;40;33;34;35;36;45;";
		if(cursorKeys.indexOf(event.keyCode + ";") == -1) {
			var input = document.getElementById(inputId) as HTMLInputElement;
			var found = false;
			var foundAtIndex = -1;
			for(var i = 0; i < select.options.length; i++) {
				if((found = select.options[i].text.toUpperCase().indexOf(input.value.toUpperCase()) == 0)) {
					foundAtIndex = i;
					break;
				}
			}
			select.selectedIndex = foundAtIndex;

			var oldValue = input.value;
			var newValue = found ? select.options[foundAtIndex].text : oldValue;
			if(newValue != oldValue) {
				if(typeof input.selectionStart != "undefined") {
					//normal browsers
					input.value = newValue;
					input.selectionStart = oldValue.length;
					input.selectionEnd = newValue.length;
					input.focus();
				}
				let dsel = (document as any).selection;
				if(dsel && dsel.createRange) {
					//IE9
					input.value = newValue;
					input.focus();
					input.select();
					var range = dsel.createRange();
					range.collapse(true);
					range.moveStart("character", oldValue.length);
					range.moveEnd("character", newValue.length);
					range.select();
				} else if((input as any).createTextRange) {
					//IE8-
					input.value = newValue;
					var rNew = (input as any).createTextRange();
					rNew.moveStart('character', oldValue.length);
					rNew.select();
				}
			}
		} else if(event.keyCode == 40) {
			select.style.display = 'inline';
			select.focus();
		}
	}

	/**
	 * Make a structure a color button.
	 */
	function colorPickerButton(btnid, inid, value, onchange) {
		$(btnid).ColorPicker({
			color: '#' + value,
			onShow: function(colpkr) {
				$(colpkr).fadeIn(500);
				return false;
			},
			onHide: function(colpkr) {
				$(colpkr).fadeOut(500);
				return false;
			},
			onChange: function(hsb, hex, rgb) {
				$(btnid + ' div').css('backgroundColor', '#' + hex);
				$(inid).val(hex);
				if(onchange)
					colorPickerOnchange(btnid, hex);
			}
		});
	}

	function colorPickerInput(inid, divid, value, onchange) {
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
				($(this) as any).ColorPickerSetColor(this.value);
			},
			onChange: function (hsb, hex, rgb) {
				$(divid).css('backgroundColor', '#' + hex);
				$(inid).val(hex);
				if(onchange)
					colorPickerOnchange(inid, hex);
			}
		});
	}

	function colorPickerDisable(id) {
		// $(id).die();
	}

	let _colorLast;

	let _colorTimer: number;

	let _colorLastID : string;

	function colorPickerOnchange(id, last) {
		if(_colorLast == last && _colorLastID == id)
			return;

		if(_colorTimer) {
			window.clearTimeout(_colorTimer);
			_colorTimer = undefined;
		}
		_colorLastID = id;
		_colorTimer = window.setTimeout("WebUI.colorPickerChangeEvent('" + id + "')", 500);
	}

	function colorPickerChangeEvent(id) {
		window.clearTimeout(_colorTimer);
		_colorTimer = undefined;
		WebUI.valuechanged('eh', id);
	}
}
