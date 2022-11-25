/// <reference types="jquery" />
/// <reference types="jqueryui" />

/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />
//import WebUI from "domui.webui.util";

namespace Plotly {
	export declare function newPlot(id, data, layout, config);
}

namespace WebUI {
	let _selectStart = undefined;

	let _popinCloseList = [];

	let _fckEditorIDs: number[] = [];

	let FCKeditor_fixLayout;


	/**
	 * Ajax Callbacks are Javascript methods that are registered by name, associated
	 * with a component, and that can be called from Server code using that name.
	 */
	class AxCallBack {
		id: string;
		componentId: String;
		callbackMethod: (...args) => void;

		constructor(id: string, comp: string, cb: (...args) => void) {
			this.id = id;
			this.componentId = comp;
			this.callbackMethod = cb;
		}
	}

	let _callbackMap = {};

	let _callbackIdCount = 1;

	/**
	 * Register a callback method that can be called at a later time by server
	 * code. The callback is identified by name.
	 */
	export function registerCallback(componentId: string, callbackMethod: (...args) => void) : string {
		let cbid = "cb" + _callbackIdCount++;
		_callbackMap[cbid] = new AxCallBack(cbid, componentId, callbackMethod);
		return cbid;
	}

	export function callCallBack(callbackId: string, ...args) {
		let cb = _callbackMap[callbackId] as AxCallBack;
		if(! cb) {
			console.log("error: Missing callback " + callbackId);
			return;
		}
		delete _callbackMap[callbackId];
		cb.callbackMethod(args);
	}

	export function oddCharAndClickCallback(nodeId, clickId): void {
		oddChar(document.getElementById(nodeId));
		document.getElementById(clickId).click();
	}

	export function oddChar(obj) {
		WebUI.copyTextToClipboard(obj.innerHTML);
	}

	export function popupMenuShow(refid, menu, mode): void {
		registerPopinClose(menu);
		let pos = $(refid).offset();
		let eWidth = $(refid).outerWidth();
		let mwidth = $(menu).outerWidth();

		let left = (pos.left);
		if(left + mwidth > screen.width)
			left = screen.width - mwidth - 10;
		let top = 3 + pos.top;

		if(mode == 'below') {
			top = pos.top + $(refid).outerHeight();
		}

		$(menu).css({
			position: 'absolute',
			zIndex: 100,
			left: left + "px",
			top: top + "px"
		});

		$(menu).hide().fadeIn();
	}

	export function popupSubmenuShow(parentId, submenu) {
		$(submenu).position({my: 'left top', at: 'center top', of: parentId});
	}

	/**
	 * Register the popup. If the mouse leaves the popup window the popup needs to send a POPINCLOSE? command; this
	 * will tell DomUI server that the popin needs to go. If an item inside the popin is clicked it should mean the
	 * popin closes too; at that point we will deregister the mouse listener to prevent sending double events.
	 *
	 * @param id
	 */
	export function registerPopinClose(id): void {
		_popinCloseList.push(id);
		// $(id).bind("mouseleave", popinMouseClose);
		if(_popinCloseList.length != 1)
			return;
		$(document.body).on("keydown", popinKeyClose);
		$(document.body).on("mousedown", popinMouseClose);
	}

	export function popinClosed(id): void {
		for(let i = 0; i < _popinCloseList.length; i++) {
			if(id === _popinCloseList[i]) {
				// console.log("popin: found popin to remove");
				//-- This one is done -> remove mouse handler.
				_popinCloseList.splice(i, 1);
				if(_popinCloseList.length == 0) {
					// console.log("Removing popin listeners because list is empty");
					$(document.body).off("keydown", popinKeyClose);
					$(document.body).off("mousedown", popinMouseClose);
				}
				return;
			}
		}
	}

	export function popinMouseClose(ev): boolean {
		// console.log("Popin mouse event caught");
		if(WebUI.isUIBlocked())							// We will get a LEAVE if the UI blocks during menu code... Ignore it
			return true;

		let target = $(ev.target);
		for(let i = 0; i < _popinCloseList.length; i++) {
			let id = _popinCloseList[i];
			let el = $(id);
			if(el) {
				//-- If event outside this popup -> close it
				if(target.closest(id).length == 0) {
					// console.log("sending popinClose for " + id);
					popinClosed(id);
					WebUI.scall(id.substring(1), "POPINCLOSE?", {});
				}
			}
		}
		return true;
	}

	export function popinCloseAll() : void {
		try {
			for(let i = 0; i < _popinCloseList.length; i++) {
				let id = _popinCloseList[i];
				let el = $(id);
				if(el) {
					el.off("mousedown", popinMouseClose);
					WebUI.scall(id.substring(1), "POPINCLOSE?", {});
				}
			}
		} finally {
			_popinCloseList = [];
			$(document.body).off("mousedown", popinMouseClose);
			$(document.body).off("keydown", popinKeyClose);
			// $(document.body).unbind("beforeclick", popinBeforeClick);
		}
	}

	export function popinKeyClose(evt): void {
		if(!evt)
			evt = window.event;
		let kk = WebUI.normalizeKey(evt);
		if(kk == 27 || kk == 27000) {
			// Prevent ESC from cancelling the AJAX call in Firefox!!
			evt.preventDefault();
			evt.cancelBubble = true;
			if(evt.stopPropagation)
				evt.stopPropagation();
			popinCloseAll();
		}
	}

	/*-------------- DataTable column --------------------*/
	export function dataTableResults(id: string, compId: string, resizeMode: string): void {
		let mode = 'flex';
		switch(resizeMode) {
			case 'FIXED':	mode = 'fit'; break;
			case 'FLEX':	mode = 'flex'; break;
			case 'OVERFLOW':mode = 'overflow'; break;
		}
		setTimeout(a => {
            $('#' +id).colResizable({
                postbackSafe: false,
				resizeMode: mode,
				partialRefresh: true,
                onResize: function(tbl) {
                    WebUI.dataTableUpdateWidths(tbl, compId);
                }
            });
		}, 500);
	}

    /**
	 * Callback which sends the new sizes of columns to the server.
     * @param evt
     * @param compId
     */
	export function dataTableUpdateWidths(evt, compId) {
		const tbl = evt.currentTarget;
		let hdrs = $(tbl).find(".ui-dt-th");
		let list = {};
		for(let i = 0; i < hdrs.length; i++) {
			let wid = (hdrs[i] as HTMLElement).style.width;
			list["column_" + hdrs[i].id] = (hdrs[i] as HTMLElement).style.width;
		}
		WebUI.scall(compId, "COLWIDTHS", list);
		console.log("Change event", tbl);
	}

	// CK editor support, map of key (id of editor) value (pair of [editor instance, assigned resize function])
	let _ckEditorMap = {};

	/**
	 * Register ckeditor for extra handling that is set in CKeditor_OnComplete.
	 */
	export function registerCkEditorId(id, ckeInstance): void {
		_ckEditorMap[id] = [ckeInstance, null];
	}

	/**
	 * Unregister ckeditor and removes handlings bound to it.
	 */
	export function unregisterCkEditorId(id): void {
		try {
			let editorBindings = _ckEditorMap[id];
			_ckEditorMap[id] = null;
			if(editorBindings && editorBindings[1]) {
				$(window).unbind('resize', editorBindings[1]);
			}
		} catch(ex) {
			WebUI.log('error in unregisterCkEditorId: ' + ex);
		}
	}

	export function CKeditor_setValue(id: string, value: string) {
		let editor = _ckEditorMap[id][0];
		if(! editor)
			return;
		editor.setData(value === undefined || value === null ? "" : value);
	}

	/**
	 * Piece of support needed for CK editor to properly fix its size, using _OnComplete handler.
	 *
	 * @param id of the area node of the editor.
	 */
	export function CKeditor_OnComplete(id): void {
		WebUI.doCustomUpdates();
		let elem = document.getElementById(id);
		let parentDiv = elem.parentNode;
		let editor = _ckEditorMap[id][0];

		//-- If width/height are both set there is no need for resize code
		if(editor.config._setWidth && editor.config._setHeight)
			return;

		let resizeFunction = function(ev) {
			try {
				// let wid = editor.config._setWidth || $(parentDiv).width() - 2;
				// let hig = editor.config._setHeight || $(parentDiv).height();
				//
				// editor.resize(wid, hig);
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
	export function initAutocomplete(inputId, selectId): void {
		let input = document.getElementById(inputId) as HTMLInputElement;
		let select = document.getElementById(selectId);
		$(input).keyup(function(event) {
			autocomplete(event, inputId, selectId);
		});
		$(select).keypress(function(event) {
			//esc hides select and prevents firing of click and blur handlers that are temporary disconnected while focus moves back to input
			let keyCode = WebUI.normalizeKey(event);
			if(keyCode == 27 || keyCode == 27000) {
				let oldVal = input.value;
				let selectOnClick = select.click;
				let selectOnBlur = select.blur;
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
	export function autocomplete(event, inputId, selectId): void {
		let select = document.getElementById(selectId) as HTMLSelectElement;
		let cursorKeys = "8;46;37;38;39;40;33;34;35;36;45;";
		if(cursorKeys.indexOf(event.keyCode + ";") == -1) {
			let input = document.getElementById(inputId) as HTMLInputElement;
			let found = false;
			let foundAtIndex = -1;
			for(let i = 0; i < select.options.length; i++) {
				if((found = select.options[i].text.toUpperCase().indexOf(input.value.toUpperCase()) == 0)) {
					foundAtIndex = i;
					break;
				}
			}
			select.selectedIndex = foundAtIndex;

			let oldValue = input.value;
			let newValue = found ? select.options[foundAtIndex].text : oldValue;
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
					let range = dsel.createRange();
					range.collapse(true);
					range.moveStart("character", oldValue.length);
					range.moveEnd("character", newValue.length);
					range.select();
				} else if((input as any).createTextRange) {
					//IE8-
					input.value = newValue;
					let rNew = (input as any).createTextRange();
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
	export function colorPickerButton(btnid, inid, value, onchange) {
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

	export function colorPickerInput(inid, divid, value, onchange) {
		$(inid).ColorPicker({
			color: '#' + value,
			flat: false,
			onShow: function(colpkr) {
				$(colpkr).fadeIn(500);
				return false;
			},
			onHide: function(colpkr) {
				$(colpkr).fadeOut(500);
				return false;
			},
			onBeforeShow: function() {
				($(this) as any).ColorPickerSetColor(this.value);
			},
			onChange: function(hsb, hex, rgb) {
				$(divid).css('backgroundColor', '#' + hex);
				$(inid).val(hex);
				if(onchange)
					colorPickerOnchange(inid, hex);
			}
		});
	}

	export function colorPickerDisable(id) {
		// $(id).die();
	}

	let _colorLast;

	let _colorTimer: number;

	let _colorLastID: string;

	export function colorPickerOnchange(id, last) {
		if(_colorLast == last && _colorLastID == id)
			return;

		if(_colorTimer) {
			window.clearTimeout(_colorTimer);
			_colorTimer = undefined;
		}
		_colorLastID = id;
		_colorTimer = window.setTimeout("WebUI.colorPickerChangeEvent('" + id + "')", 500);
	}

	export function colorPickerChangeEvent(id): void {
		window.clearTimeout(_colorTimer);
		_colorTimer = undefined;
		WebUI.valuechanged('eh', id);
	}

	export function flare(id): void {
		$('#' + id).fadeIn('fast', function() {
			$('#' + id).delay(500).fadeOut(1000, function() {
				$('#' + id).remove();
			});
		});
	}

	export function flareStay(id): void {
		$('#' + id).fadeIn('fast', function() {
			$('body,html').bind('mousemove.' + id, function(e) {
				$('body,html').unbind('mousemove.' + id);
				$('#' + id).delay(500).fadeOut(1000, function() {
					$('#' + id).remove();
				});
			});
		});
	}

	export function flareStayCustom(id, delay, fadeOut): void {
		$('#' + id).fadeIn('fast', function() {
			$('body,html').bind('mousemove.' + id, function(e) {
				$('body,html').unbind('mousemove.' + id);
				$('#' + id).delay(delay).fadeOut(fadeOut, function() {
					$('#' + id).remove();
				});
			});
		});
	}

	//piece of support needed for FCK editor to properly fix heights in IE8+
	export function FCKeditor_OnComplete(editorInstance) {
		if(WebUI.isIE8orNewer()) {
			for(let i = 0; i < _fckEditorIDs.length; i++) {
				let fckId = _fckEditorIDs[i];
				let fckIFrame = document.getElementById(fckId + '___Frame') as HTMLIFrameElement;
				if(fckIFrame) {
					$(fckIFrame.contentWindow.window).bind('resize', function() {
						FCKeditor_fixLayout(fckIFrame, fckId);
					});
					$(fckIFrame.contentWindow.window).trigger('resize');
				}
			}
		}
	}

	export function scrollableTableReset(id, tblid) {
		initScrollableTable(id, tblid);

		let tbl = $('#' + tblid);
		let container = $('#' + id);
		// // (tbl as any).floatThead('reflow');
		// WebUI.doCustomUpdates();
		// $.dbg('recreate');

		//tbl.floatThead('destroy');
		//tbl.floatThead({
		//	scrollContainer: function() {
		//		return container;
		//	}
		//});

		container.scrollTop(0);
	}

	export function initScrollableTable(id, tblid) {
		let container = $('#' + id);
		let tbl = $('#' + tblid);
		WebUI.doCustomUpdates();

		// (tbl as any).floatThead({
		// 	scrollContainer: function() {
		// 		return container;
		// 	},
		// 	getSizingRow: function($table) { // this is only called when using IE, we need any row without colspan, see http://mkoryak.github.io/floatThead/examples/row-groups/
		// 		let rows = $table.find('tbody tr:visible').get();
		// 		for(let i = 0; i < rows.length; i++) {
		// 			let cells = $(rows[i]).find('td');
		// 			let isInvalidRow = false;
		// 			for(let i = 0; i < cells.get().length; i++) {
		// 				if(Number($(cells[i]).attr('colspan')) > 1) {
		// 					isInvalidRow = true;
		// 				}
		// 			}
		// 			if(!isInvalidRow) {
		// 				return cells;
		// 			}
		// 		}
		// 		if(rows.length > 0) {
		// 			return $(rows[0]).find('td'); //as fallback we just return first row cells
		// 		} else {
		// 			return null; //or nothing -> but this should not be possible since getSizingRow is called only on table with rows
		// 		}
		// 	}
		// });
		container.scroll(function() {
			let bh = $(container).height();
			let st = $(container).scrollTop();
			let tbl = $('#' + id + " tbody");
			let th = tbl.height();
			let left = tbl.height() - bh - st;
			$.dbg("scrolling: bodyheight=" + bh + " scrolltop=" + st + " tableheight=" + th + " left=" + left);

			if(left > 100) {
				//$.dbg("Scrolling: area left="+left);
				return;
			}

			let lastRec = tbl.find("tr[lastRow]");
			if(lastRec.length != 0) {
				//$.dbg("scrolling: lastrec found");
				return;
			}
			WebUI.scall(id, "LOADMORE", {});
		});
	}

	export function plotlyComponent(id: string) {
		let pnl = $('#' + id);
		pnl.addClass('ui-plotly-empty');
		DomUI.jsoncall(id, {}, function(response) {
			let data = response['data'];
			let layout = response['layout'];
			if(data != null && layout != null) {
				let config = {responsive: true};
				Plotly.newPlot(id, data, layout, config);
			}
		});
	}

	export function aceMakeResizable(id_editor: string, id_bar: string) {
		let editor = window[id_editor];
		if(editor == null)
			return;

		let draggingAceEditor = window['draggingAceEditor'];
		if(null == draggingAceEditor) {
			draggingAceEditor = {};
			window['draggingAceEditor'] = draggingAceEditor;
		}
		let id_dragbar = '#' + id_bar;
		// let id_wrapper = '#' + id_editor + '_wrapper';
		let wpoffset = 0;
		draggingAceEditor[id_editor] = false;

		$(id_dragbar).mousedown(function(e) {
			e.preventDefault();

			draggingAceEditor[id_editor] = true;

			let _editor = $('#' + id_editor);
			let top_offset = _editor.offset().top - wpoffset;

			// Set editor opacity to 0 to make transparent so our wrapper div shows
			_editor.css('opacity', 0);

			// handle mouse movement
			$(document).mousemove(function(e){
				let actualY = e.pageY - wpoffset;
				// editor height
				let eheight = actualY - top_offset;

				// Set wrapper height
				_editor.parent().css('height', eheight);

				// Set dragbar opacity while dragging (set to 0 to not show)
				$(id_dragbar).css('opacity', 0.15);
			});
		});

		$(document).mouseup(function(e){
			let draggingAceEditor = window['draggingAceEditor'];
			if(null == draggingAceEditor) {
				draggingAceEditor = {};
				window['draggingAceEditor'] = draggingAceEditor;
			}

			if(draggingAceEditor[id_editor]) {
				let ctx_editor = $('#' + id_editor);

				let actualY = e.pageY - wpoffset;
				let top_offset = ctx_editor.offset().top - wpoffset;
				let eheight = actualY - top_offset;

				$( document ).unbind('mousemove');

				// Set dragbar opacity back to 1
				$(id_dragbar).css('opacity', 1);

				// Set height on actual editor element, and opacity back to 1
				ctx_editor.css('height', eheight).css('opacity', 1);
				ctx_editor.parent().css("height", eheight);

				// Trigger ace editor resize()
				editor.resize();

				draggingAceEditor[id_editor] = false;
			}
		});
	}

	class closeOnClick {
		_id: string;
		private _clickHandler: () => void;
		private _keyUpHandler: (t: any) => any;

		constructor(id: string) {
			this._id = id;
			var clickHandler = this._clickHandler = $.proxy(this.closeMenu, this);
			 $(document).click(clickHandler);
			var keyUpHandler = this._keyUpHandler = $.proxy(this.buttonHandler, this);
			$(document).keyup(keyUpHandler);
			$('#' + id).data('inst', this);
		}

		closeMenu() {
			this.unbind();
			WebUI.scall(this._id, "CLOSEMENU?", {});
		}

		unbind() {
			$(document).unbind("click", this._clickHandler);
			$(document).unbind("keyup", this._keyUpHandler);
		}

		markClosed(id) {
			var inst = $('#' + id).data('inst');
			if(inst) {
				inst.unbind();
			}
		}

		isInputTagEvent(event) {
			var src = event.srcElement;
			if(src) {
				var tn = src.tagName.toUpperCase();
				if(tn === 'INPUT' || tn == 'SELECT' || tn == "TEXTAREA")
					return true;
			}
			return false;
		}

		buttonHandler(event) {
			if (this.isInputTagEvent(event))
				return;

			if (event.which == 27) {				// escape
				this.closeMenu();
			}
		}
	}

	namespace DbPerformance {
		export function post(id: string, sessionid: string) {
			$(document).ready(function() {
				setTimeout(function() {
					$.get((window as any).DomUIappURL + "nl.itris.vp.parts.DbPerf.part?requestid=" + sessionid, function(data) {
						//-- Insert the div as the last in the body
						$('#' + id).html(data);
						$(".vp-lspf").draggable({zIndex: 100, handle: '.vp-lspf-ttl'});
						$(".vp-lspf-close").click(function() {
							$(".vp-lspf").hide();
						});

					});
				}, 500);
			})
		}
	}


}
