/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />
// declare module "domui.webui.util";

namespace WebUI {
	export let _T  : any = {};

	export function definePageName(pn: string): void {
		$(document.body).attr("pageName", pn);
	}

	export function log(...args): void {
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
	export function normalizeKey(evt: any): number {
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

	export function isNumberKey(evt: any): boolean {
		//-- onKeyPress event: use keyCode
		let keyCode = normalizeKey(evt);
		//$.dbg("kp: norm="+keyCode+", keyCode="+evt.keyCode+", chc="+evt.charCode+", which="+evt.which, evt);
		return (keyCode >= 1000 || (keyCode >= 48 && keyCode <= 57) || keyCode == 45);
	}

	export function isFloatKey(evt: any): boolean {
		let keyCode = normalizeKey(evt);
		//alert('keycode='+evt.keyCode+", charCode="+evt.charCode+", which="+evt.which+", norm="+keyCode);
		return (keyCode >= 1000 || keyCode == 0x2c || keyCode == 0x2e || (keyCode >= 48 && keyCode <= 57) || keyCode == 45);
	}

	export function delayedSetAttributes(): void {
		if(arguments.length < 3 || ((arguments.length & 1) != 1)) {
			alert('internal: odd call to delayedSetAttributes: ' + arguments.length);
			return;
		}
		let n = document.getElementById(arguments[0]);
		if(n == undefined)
			return;
//		alert('Node is '+arguments[0]);
		//-- Now set pair values
		for(let i = 1; i < arguments.length; i += 2) {
			try {
				n[arguments[i]] = arguments[i + 1];
			} catch(x) {
				alert('Failed to set javascript property ' + arguments[i] + ' to ' + arguments[i + 1] + ": " + x);
			}
		}
	}

	export function focus(id: string): void {
		let n = document.getElementById(id);
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

	/**
	 * Take the page URL, remove the CID, then reload the page.
	 */
	export function refreshPage(): void {
		let url = window.location.href;
		let ix1 = url.indexOf("$cid=");
		if(ix1 > 0) {
			let ix2 = url.indexOf("&", ix1);
			if(ix2 > ix1) {
				url = url.substring(0, ix1) + url.substring(ix2 + 1);
			} else {
				url = url.substring(0, ix1);
			}
			window.location.href = url;
		}
	}

	export function getPostURL(): string {
		let p = window.location.href;
		let ix = p.indexOf('?');
		if(ix != -1)
			p = p.substring(0, ix); // Discard query string.
		return p;
	}

	export function getObituaryURL(): string {
		let u = getPostURL();
		let ix = u.lastIndexOf('.');
		if(ix < 0)
			throw "INVALID PAGE URL";
		return u.substring(0, ix) + ".obit";
	}

	export function openWindow(url: string, name: string, par: any): boolean {
		let h = undefined;
		try {
			h = window.open(url, name, par);
		} catch(x) {
			alert("Got popup exception: " + x);
		}
		if(!h)
			alert(WebUI._T.sysPopupBlocker);
		return false;
	}

	export function postURL(path: string, name: string, params, target): void {
		let form = document.createElement("form");
		form.setAttribute("method", "post");
		form.setAttribute("action", path);
		if(null != target) {
			form.setAttribute("target", target);
		}

		for(let key in params) {
			if(params.hasOwnProperty(key)) {
				let hiddenField = document.createElement("input");
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
	export function isBrowserClosed(e): boolean {
		try {
			// -- ie does not work as usual.
			if(window.event) {
				let e = window.event as any;
				alert('wcy=' + e.clientY + ", wcx="
					+ e.clientX + ", dw="
					+ document.documentElement.clientWidth + ", screentop="
					+ self.screenTop);
				if(e.clientY < 0 && (e.clientX > (document.documentElement.clientWidth - 5) || e.clientX < 15))
					return true;
			}
		} catch(x) {
		}

		try { // Firefox part works properly.
			if(window.innerWidth == 0 && window.innerHeight == 0)
				return true;
		} catch(x) {
		}

		return false;
	}


	export function toClip(value: any): void {
		let w = (window as any);
		if(w.clipboardData) {
			// the IE-way
			w.clipboardData.setData("Text", value);
		} else if(w.netscape) {
			if(value.createTextRange) {
				let range = value.createTextRange();
				if(range /*&& BodyLoaded == 1 */)
					range.execCommand('Copy');
			} else {
				let flashcopier = 'flashcopier';
				if(!document.getElementById(flashcopier)) {
					let divholder = document.createElement('div');
					divholder.id = flashcopier;
					document.body.appendChild(divholder);
				}
				document.getElementById(flashcopier).innerHTML = '';
				let divinfo = '<embed src="$js/_clipboard.swf" FlashVars="clipboard=' + encodeURIComponent(value) + '" width="0" height="0" type="application/x-shockwave-flash"></embed>';
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
	export function format(message: string, ...rest): string {
		for(let i = 1; i < arguments.length; i++) {
			message = message.replace("{" + (i - 1) + "}", arguments[i]);
		}
		return message;
	}

	export function findParentOfTagName(node: any, type: string): any {
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
	export function disableSelection(node: any): void {
		node.onselectstart = function() {
			return false;
		};
		node.unselectable = "on";
		node.style.MozUserSelect = "none";
		node.style.cursor = "default";
	}

	export function disableSelect(id: string) {
		if($.browser.msie) {
			$('#' + id).disableSelection();
		} else {
			$('#' + id).addClass("ui-selection-disable");
		}
	}

	export function enableSelect(id: string) {
		if($.browser.msie) {
			$('#' + id).enableSelection();
		} else {
			$('#' + id).removeClass("ui-selection-disable");
		}
	}

	export function nearestID(elem: HTMLElement): any {
		while(elem) {
			if(elem.id)
				return elem.id;
			elem = elem.parentNode as HTMLElement;
		}
		return undefined;
	}

	//We need to re-show element to force IE7 browser to recalculate correct height of element. This must be done to fix some IE7 missbehaviors.
	export function refreshElement(id: string): void {
		let elem = document.getElementById(id);
		if(elem) {
			$(elem).hide();
			$(elem).show(1); //needs to be done on timeout/animation, otherwise it still fails to recalculate...
		}
	}

	export class Point {
		x: number;
		y: number;

		constructor(x, y) {
			this.x = x;
			this.y = y;
		}
	}

	export class Rect {
		bx: number;
		by: number;
		ex: number;
		ey: number;

		constructor(_bx, _by, _ex, _ey) {
			this.bx = _bx;
			this.ex = _ex;
			this.by = _by;
			this.ey = _ey;
		}
	}

	export function getAbsolutePosition(obj): Point {
		var top = 0, left = 0;
		while(obj) {
			top += obj.offsetTop;
			left += obj.offsetLeft;
			obj = obj.offsetParent;
		}
		return new Point(left, top);
	}

	/**
	 * None of the "standard" JS libraries like Rico or Prototype have code that
	 * actually <i>works</i> to get the actual <i>page or absolute</i>
	 * position of elements when scrolling is used. All of them unconditionally
	 * add scroll offsets to the relative positions but scrolling *will* cause
	 * items to become *invisible* because they are scrolled out of view. The
	 * calls here obtain a location for elements taking scrolling into account,
	 * and they will return null if the item is not visible at all.
	 */
	export function getAbsScrolledPosition(el): Rect {
		// -- Calculate the element's current offseted locations
		var bx = el.offsetLeft || 0;
		var by = el.offsetTop || 0;
		var ex = bx + el.offsetWidth;
		var ey = by + el.offsetHeight;

		var el = el.parentNode;
		while(el != null) {
			if(el.clientHeight != null) {
				// -- Check the current location within the parent's bounds.
				if(by < el.scrollTop)
					by = el.scrollTop;
				if(bx < el.scrollLeft)
					bx = el.scrollLeft;
				if(bx >= ex || by >= ey) // Not visible
					return null;

				// -- Check the end coordinates.
				var vey = el.scrollTop + el.clientHeight;
				var vex = el.scrollLeft + el.clientWidth;
				if(ex > vex)
					ex = vex;
				if(ey > vey)
					ey = vey;
				if(by >= ey || bx >= ex) // Past the viewport's bounds?
					return null;

				// -- This much of the rectangle fits the viewport. Now make the
				// position absolute within the viewport.
				by -= el.scrollTop;
				ey -= el.scrollTop;
				bx -= el.scrollLeft;
				ex -= el.scrollLeft;

				by += el.offsetTop;
				ey += el.offsetTop;
				bx += el.offsetLeft;
				ex += el.offsetLeft;
			}
			el = el.parentNode;
		}
		return new Rect(bx, by, ex, ey);
	}

	//Use this to make sure that item would be visible inside parent scrollable area. It uses scroll animation. In case when item is already in visible part, we just do single blink to gets user attention ;)
	export function scrollMeToTop(elemId: string, selColor: string, offset: number): void {
		let elem = document.getElementById(elemId);
		if(!elem) {
			return;
		}
		let parent = elem.parentNode as any;
		if(!parent) {
			return;
		}
		if(parent.scrollHeight > parent.offsetHeight) { //if parent has scroll
			let elemPos = $(elem).position().top;
			if(elemPos > 0 && elemPos < parent.offsetHeight) {
				//if elem already visible -> just do one blink
				if(selColor) {
					let oldColor = $(elem).css('background-color');
					$(elem).animate({backgroundColor: selColor}, "slow", function() {
						$(elem).animate({backgroundColor: oldColor}, "fast");
					});
				}
			} else {
				//else scroll parent to show me at top
				let newPos = $(elem).position().top + parent.scrollTop;
				if($.browser.msie && parseInt($.browser.version) < 11) {
					if($(elem).height() == 0) {
						newPos = newPos - 15; //On IE browsers older than 11 we need this correction :¬|
					}
				}
				if(offset) {
					newPos = newPos - offset;
				}
				$(parent).animate({scrollTop: newPos}, 'slow');
			}
		}
	}

	//Use this to make sure that option in dropdown would be visible. It needs fix only in FF sinve IE would always make visible selected option.
	export function makeOptionVisible(elemId: string, offset: number): void {
		if($.browser.msie) {
			//IE already fix this... we need fix only for FF and other browsers
			return;
		}
		let elem = document.getElementById(elemId);
		if(!elem) {
			return;
		}
		let parent = elem.parentNode as any;
		if(!parent) {
			return;
		}
		if(parent.scrollHeight > parent.offsetHeight) { //if parent has scroll
			let elemPos = $(elem).position().top;
			//if elem is not currenlty visible
			if(elemPos <= 0 || elemPos >= parent.offsetHeight) {
				//else scroll parent to show me at top
				let newPos = elemPos + parent.scrollTop;
				if(offset) {
					newPos = newPos - offset;
				}
				$(parent).animate({scrollTop: newPos}, 'slow');
			}
		}
	}

	export function truncateUtfBytes(str: string, nbytes: number): number {
		//-- Loop characters and calculate running length
		let bytes = 0;
		let length = str.length;
		for(let ix = 0; ix < length; ix++) {
			let c = str.charCodeAt(ix);
			if(c < 0x80)
				bytes++;
			else if(c < 0x800)
				bytes += 2;
			else
				bytes += 3;
			if(bytes > nbytes)
				return ix;
		}
		return length;
	}

	export function utf8Length(str: string): number {
		let bytes = 0;
		let length = str.length;
		for(let ix = 0; ix < length; ix++) {
			let c = str.charCodeAt(ix);
			if(c < 0x80)
				bytes++;
			else if(c < 0x800)
				bytes += 2;
			else
				bytes += 3;
		}
		return bytes;
	}

	/** In tables that have special class selectors that might cause text-overflow we show full text on hover */
	export function showOverflowTextAsTitle(id: string, selector: string): boolean {
		let root = $("#" + id);
		if(root) {
			root.find(selector).each(function() {
				if(this.offsetWidth < this.scrollWidth) {
					let $this = $(this);
					$this.attr("title", $this.text());
				}
			});
		}
		return true;
	}

	export function replaceBrokenImageSrc(id: string, alternativeImage: string): void {
		$('img#' + id).error(function() {
			$(this).attr("src", alternativeImage);
		});
	}

	export function deactivateHiddenAccessKeys(windowId: string): void {
		$('button').each(function(index) {
			let iButton = $(this);
			if(isButtonChildOfElement(iButton, windowId)) {
				let oldAccessKey = $(iButton).attr('accesskey');
				if(oldAccessKey != null) {
					$(iButton).attr('accesskey', $(windowId).attr('id') + '~' + oldAccessKey);
				}
			}
		});
	}

	export function reactivateHiddenAccessKeys(windowId: string): void {
		$("button[accesskey*='" + windowId + "~']").each(function(index) {
			let attr = $(this).attr('accesskey') as string;
			let accessKeyArray = attr.split(windowId + '~');
			$(this).attr('accesskey', accessKeyArray[accessKeyArray.length - 1]);
		});
	}

	export function isButtonChildOfElement(buttonId: any, windowId: string): boolean {
		return $(buttonId).parents('#' + $(windowId).attr('id')).length == 0;
	}

	/** ***************** Stretch elemnt height. Must be done via javascript. **************** */
	export function stretchHeight(elemId: string): void {
		let elem = document.getElementById(elemId);
		if(!elem) {
			return;
		}
		stretchHeightOnNode(elem);
	}

	export function stretchHeightOnNode(elem: HTMLElement): void {
		let elemHeight = $(elem).height();
		let totHeight = 0;
		$(elem).siblings().each(function(index, node) {
			//do not count target element and other siblings positioned absolute or relative to parent in order to calculate how much space is actually taken / available
			if(node != elem && $(node).css('position') == 'static' && ($(node).css('float') == 'none' || $(node).css('width') != '100%' /* count in floaters that occupies total width */)) {
				//In IE7 hidden nodes needs to be additionaly excluded from count...
				if(!($(node).css('visibility') == 'hidden' || $(node).css('display') == 'none')) {
					//totHeight += node.offsetHeight;
					totHeight += $(node).outerHeight(true);
				}
			}
		});
		let elemDeltaHeight = $(elem).outerHeight(true) - $(elem).height(); //we need to also take into account elem paddings, borders... So we take its delta between outter and inner height.
		if(WebUI.isIE8orIE8c()) {
			//from some reason we need +1 only for IE8!
			elemDeltaHeight = elemDeltaHeight + 1;
		}
		$(elem).height($(elem).parent().height() - totHeight - elemDeltaHeight);
		if($.browser.msie && $.browser.version.substring(0, 1) == "7") {
			//we need to special handle another IE7 muddy hack -> extra padding-bottom that is added to table to prevent non-necesarry vertical scrollers
			if(elem.scrollWidth > elem.offsetWidth) {
				$(elem).height($(elem).height() - 20);
				//show hidden vertical scroller if it is again needed after height is decreased.
				if($(elem).css('overflow-y') == 'hidden') {
					if(elem.scrollHeight > elem.offsetHeight) {
						$(elem).css({'overflow-y': 'auto'});
					}
				}
				return;
			}
		}
	}


	/** Dynamically loading stylesheets and javascript files (Header Contributer delta's) **/
	/**
	 * Load the specified stylesheet by creating a script tag and inserting it @ head.
	 */
	export function loadStylesheet(path) {
		var head = document.getElementsByTagName("head")[0];
		if(! head)
			throw "Headless document!?";
		var link = document.createElement('link');
		link.type = 'text/css';
		link.rel = 'stylesheet';
		link.href = path;
		link.media = 'screen';
		head.appendChild(link);
	}

	export function loadJavascript(path) {
		var head = document.getElementsByTagName("head")[0];
		if(! head)
			throw "Headless document!?";
		var scp = document.createElement('script');
		scp.type = 'text/javascript';
		scp.src = path;
		head.appendChild(scp);
	}

	/** Prevents default action to be executed if IE11 is detected */
	export function preventIE11DefaultAction(e){
		if((navigator.userAgent.match(/Trident\/7\./))){
			e.preventDefault();
		}
	}

	export function preventSelection() : boolean {
		return false;
	}

}

