/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
namespace WebUIStatic {
	function definePageName(pn: string): void {
		$(document.body).attr("pageName", pn);
	}

	function log(...args): void {
		$.dbg.apply(this, args);
	}

	/**
	 * Create a curried function containing a 'this' and a fixed set of elements.
	 */
	function curry(scope: any, fn: Function): Function {
		scope = scope || window;
		let args = [];
		for(let i = 2, len = arguments.length; i < len; ++i) {
			args.push(arguments[i]);
		}
		return function() {
			fn.apply(scope, args);
		};
	}

	/**
	 * Embeds the "this" and any *partial* parameters to the function.
	 */
	function pickle(scope: any, fn: Function): Function {
		scope = scope || window;
		let args = [];
		for(let i = 2, len = arguments.length; i < len; ++i) {
			args.push(arguments[i]);
		}
		return function() {
			let nargs = [];
			for(let i = 0, len = args.length; i < len; i++) // Append all args added to pickle
				nargs.push(args[i]);
			for(let i = 0, len = arguments.length; i < len; i++) // Append all params of the actual function after it
				nargs.push(arguments[i]);
			fn.apply(scope, nargs);
		};
	}

	/*
	 * IE/FF compatibility: IE only has the 'keycode' field, and it always hides
	 * all non-input like arrows, fn keys etc. FF has keycode which is used ONLY
	 * for non-input keys and charcode for input.
	 */
	function normalizeKey(evt: any): number {
		if($.browser.mozilla) {
			if(evt.charCode > 0)
				return evt.charCode;
			return evt.keyCode * 1000;
		}

		if(evt.charCode != undefined) {
			if(evt.charCode == evt.keyCode)
				return evt.charCode;
			if(evt.keyCode > 0)
				return evt.keyCode * 1000; 	// Firefox high # for cursor crap
			return evt.charCode;
		}
		return evt.keyCode;					// Return IE charcode
	}

	function isNumberKey(evt: any): boolean {
		//-- onKeyPress event: use keyCode
		var keyCode = normalizeKey(evt);
		//$.dbg("kp: norm="+keyCode+", keyCode="+evt.keyCode+", chc="+evt.charCode+", which="+evt.which, evt);
		return (keyCode >= 1000 || (keyCode >= 48 && keyCode <= 57) || keyCode == 45);
	}

	function isFloatKey(evt: any): boolean {
		var keyCode = normalizeKey(evt);
		//alert('keycode='+evt.keyCode+", charCode="+evt.charCode+", which="+evt.which+", norm="+keyCode);
		return (keyCode >= 1000 || keyCode == 0x2c || keyCode == 0x2e || (keyCode >= 48 && keyCode <= 57) || keyCode == 45);
	}

	function delayedSetAttributes(): void {
		if(arguments.length < 3 || ((arguments.length & 1) != 1)) {
			alert('internal: odd call to delayedSetAttributes: ' + arguments.length);
			return;
		}
		var n = document.getElementById(arguments[0]);
		if(n == undefined)
			return;
//		alert('Node is '+arguments[0]);
		//-- Now set pair values
		for(var i = 1; i < arguments.length; i += 2) {
			try {
				n[arguments[i]] = arguments[i + 1];
			} catch(x) {
				alert('Failed to set javascript property ' + arguments[i] + ' to ' + arguments[i + 1] + ": " + x);
			}
		}
	}

	function focus(id: string): void {
		var n = document.getElementById(id);
		if(n) {
			if($.browser.msie) {
				setTimeout(function() {
					try {
						$('body').focus();
						n.focus();
					} catch(e) { /*just ignore */
					}
				}, 100); //Due to IE bug, we need to set focus on timeout :( See http://www.mkyong.com/javascript/focus-is-not-working-in-ie-solution/
			} else {
				try {
					n.focus();
				} catch(e) {
					//-- ignore
				}
			}
		}
	}


	function getPostURL() : string {
		var p = window.location.href;
		var ix = p.indexOf('?');
		if(ix != -1)
			p = p.substring(0, ix); // Discard query string.
		return p;
	}

	function getObituaryURL() : string {
		var u = getPostURL();
		var ix = u.lastIndexOf('.');
		if(ix < 0)
			throw "INVALID PAGE URL";
		return u.substring(0, ix) + ".obit";
	}

	function openWindow(url: string, name: string, par: any) : boolean {
		try {
			var h = window.open(url, name, par);
		} catch(x) {
			alert("Got popup exception: "+x);
		}
		if (!h)
			alert(WebUI._T.sysPopupBlocker);
		return false;
	}

	function postURL(path: string, name: string, params, target) : void {
		var form = document.createElement("form");
		form.setAttribute("method","post");
		form.setAttribute("action", path);
		if (null != target){
			form.setAttribute("target", target);
		}

		for (var key in params) {
			if (params.hasOwnProperty(key)) {
				var hiddenField = document.createElement("input");
				hiddenField.setAttribute("type", "hidden");
				hiddenField.setAttribute("name", key);
				hiddenField.setAttribute("value", params[key]);
				form.appendChild(hiddenField);
			}
		}
		document.body.appendChild(form);
		form.submit();
	}

	/**
	 * Do not use- kept for when we find a solution to IE's close problem.
	 */
	function isBrowserClosed(e) : boolean {
		try {
			// -- ie does not work as usual.
			if(window.event) {
				let e = window.event as any;
				alert('wcy=' + e.clientY + ", wcx="
					+ e.clientX + ", dw="
					+ document.documentElement.clientWidth + ", screentop="
					+ self.screenTop);
				if (e.clientY < 0 && (e.clientX > (document.documentElement.clientWidth - 5) || e.clientX < 15))
					return true;
			}
		} catch (x) {
		}

		try { // Firefox part works properly.
			if (window.innerWidth == 0 && window.innerHeight == 0)
				return true;
		} catch (x) {
		}

		return false;
	}


	function toClip(value: any) : void {
		let w = (window as any);
		if(w.clipboardData) {
			// the IE-way
			w.clipboardData.setData("Text", value);
		} else if(w.netscape) {
			if(value.createTextRange) {
				var range = value.createTextRange();
				if(range /*&& BodyLoaded == 1 */)
					range.execCommand('Copy');
			} else {
				var flashcopier = 'flashcopier';
				if(!document.getElementById(flashcopier)) {
					var divholder = document.createElement('div');
					divholder.id = flashcopier;
					document.body.appendChild(divholder);
				}
				document.getElementById(flashcopier).innerHTML = '';
				var divinfo = '<embed src="$js/_clipboard.swf" FlashVars="clipboard=' + encodeURIComponent(value) + '" width="0" height="0" type="application/x-shockwave-flash"></embed>';
				document.getElementById(flashcopier).innerHTML = divinfo;
			}
		}
	}

	/**
	 * Format a NLS message containing {0} and {1} markers and the like into
	 * a real message.
	 * @param message
	 * @returns
	 */
	function format(message: string) : string {
		for(var i = 1; i < arguments.length; i++) {
			message = message.replace("{"+(i-1)+"}", arguments[i]);
		}
		return message;
	}

	function findParentOfTagName(node: any, type: string) : any {
		while(node != null) {
			node = node.parentNode;
			if(node.tagName == type)
				return node;
		}
		return null;
	}

	/**
	 * @deprecated This is incorrect! Use disableSelect below!
	 * Disable selection on the given element.
	 */
	function disableSelection(node: any) : void {
		node.onselectstart = function() {
			return false;
		};
		node.unselectable = "on";
		node.style.MozUserSelect = "none";
		node.style.cursor = "default";
	}

	function disableSelect(id: string) {
		if($.browser.msie) {
			$('#' + id).disableSelection();
		} else {
			$('#' + id).addClass("ui-selection-disable");
		}
	}

	function enableSelect(id: string) {
		if($.browser.msie) {
			$('#' + id).enableSelection();
		} else {
			$('#' + id).removeClass("ui-selection-disable");
		}
	}




}

