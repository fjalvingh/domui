/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />
//import WebUI from "domui.webui.util";

namespace WebUI {
	let _customUpdatesContributors = $.Callbacks("unique");

	let _customUpdatesContributorsTimerID: number = null;

	let _browserChecked = false;

	/**
	 * registers function that gets called after doCustomUpdates sequence of calls ends, with 500 delay - doCustomUpdates can trigger new doCustomUpdates etc...
	 * @param contributorFunction
	 */
	export function registerCustomUpdatesContributor(contributorFunction: Function): void {
		_customUpdatesContributors.add(contributorFunction);
	}

	export function unregisterCustomUpdatesContributor(contributorFunction: Function): void {
		_customUpdatesContributors.remove(contributorFunction);
	}

	export function doCustomUpdates(): void {
		$('.floatThead-wrapper').each(
			function(index, node) {
				$(node).attr('stretch', $(node).find('>:first-child').attr('stretch'));
			}
		);
		$('[stretch=true]').doStretch();
		$('.ui-dt, .ui-fixovfl').fixOverflow();
		$('input[marker]').setBackgroundImageMarker();

		//-- Limit textarea size on paste events
		$("textarea[mxlength], textarea[maxbytes]")
			.unbind("input.domui")
			.unbind("propertychange.domui")
			.bind('input.domui propertychange.domui', function() {
				let maxLength = attrNumber(this, 'mxlength');				// Use mxlength because Chrome improperly implements maxlength (issue 252613)
				let maxBytes = attrNumber(this, 'maxbytes');
				let val = $(this).val() as string;
				let newlines = (val.match(/\r\n/g) || []).length;				// Count the #of 2-char newlines, as they will be replaced by 1 newline character
				if(maxBytes < 0) {
					if(maxLength < 0)
						return;
				} else if(maxLength < 0) {
					maxLength = maxBytes;
				}

				if(val.length + newlines > maxLength) {
					val = val.substring(0, maxLength - newlines);
					$(this).val(val);
				}
				if(maxBytes > 0) {
					let cutoff = WebUI.truncateUtfBytes(val, maxBytes);
					if(cutoff < val.length) {
						val = val.substring(0, cutoff);
						$(this).val(val);
					}
				}
			});

		//-- Limit textarea size on key presses
		$("textarea[mxlength], textarea[maxbytes]")
			.unbind("keypress.domui")
			.bind('keypress.domui', function(evt) {
				if(evt.which == 0 || evt.which == 8)
					return true;

				//-- Is the thing too long already?
				let maxLength = attrNumber(this, 'mxlength');				// Use mxlength because Chrome improperly implements maxlength (issue 252613)
				let maxBytes = attrNumber(this, 'maxbytes');
				let val = $(this).val() as string;
				let newlines = (val.match(/\r\n/g) || []).length;				// Count the #of 2-char newlines, as they will be replaced by 1 newline character
				if(maxBytes < 0) {
					if(maxLength < 0)
						return true;
				} else if(maxLength < 0) {
					maxLength = maxBytes;
				}
				if(val.length - newlines >= maxLength)							// Too many chars -> not allowed
					return false;
				if(maxBytes > 0) {
					let bytes = WebUI.utf8Length(val);
					if(bytes >= maxBytes)
						return false;
				}
				return true;
			});

		//custom updates may fire several times in sequence, se we fire custom contributors only after it gets steady for a while (500ms)
		if(_customUpdatesContributorsTimerID) {
			window.clearTimeout(_customUpdatesContributorsTimerID);
			_customUpdatesContributorsTimerID = null;
		}
		_customUpdatesContributorsTimerID = window.setTimeout(function() {
			try {
				_customUpdatesContributors.fire()
			} catch(ex) {
			}
		}, 500);
		//$('.ui-dt-ovflw-tbl').floatThead('reflow');
	}

	function attrNumber(elem, name: string): number {
		let val = $(elem).attr(name);
		if(typeof val == 'undefined')
			return -1;
		return Number(val);
	}

	export function onDocumentReady(): void {
		checkBrowser();
		WebUI.handleCalendarChanges();
		if((window as any).DomUIDevel)
			handleDevelopmentMode();
		doCustomUpdates();
	}

	function checkBrowser(): void {
		if(this._browserChecked)
			return;
		this._browserChecked = true;

		//-- We do not support IE 7 and lower anymore.
		if($.browser.msie && $.browser.majorVersion < 8) {
			//-- Did we already report that warning this session?
			if($.cookie("domuiie") == null) {
				alert(WebUI.format(WebUI._T.sysUnsupported, $.browser.majorVersion));
				$.cookie("domuiie", "true", {});
			}
		}
	}

	let _debugLastKeypress: number;
	let _debugMouseTarget: HTMLElement;

	export function handleDevelopmentMode(): void {
		$(document).bind("keydown", function(e) {
			if(e.keyCode != 192)
				return;

			let t = new Date().getTime();
			if(!_debugLastKeypress || (t - _debugLastKeypress) > 250) {
				_debugLastKeypress = t;
				return;
			}

			//-- Send a DEBUG command to the server, indicating the current node below the last mouse move....
			let id = WebUI.nearestID(_debugMouseTarget);
			if(!id) {
				id = document.body.id;
			}

			WebUI.scall(id, "DEVTREE", {});
		});
		$(document.body).bind("mousemove", function(e) {
//			if(WebUI._NOMOVE)
//				return;
//			console.debug("move ", e);
			_debugMouseTarget = e.target; // e.srcElement || e.originalTarget;
		});
	}

	/** *************** Debug thingy - it can be used internaly for debuging javascript ;) ************** */
	export function debug(debugId: string, posX: number, posY: number, debugInfoHtml: any) {
		//Be aware that debugId must not start with digit when using FF! Just lost 1 hour to learn this...
		if("0123456789".indexOf(debugId.charAt(0)) > -1) {
			alert("debugId(" + debugId + ") starts with digit! Please use different one!");
		}
		let debugPanel = document.getElementById(debugId);
		if(null == debugPanel) {
			debugPanel = document.createElement(debugId);
			$(debugPanel).attr('id', debugId);
			$(debugPanel).css('position', 'absolute');
			$(debugPanel).css('marginLeft', 0);
			$(debugPanel).css('marginTop', 0);
			$(debugPanel).css('background-color', 'yellow');
			$(debugPanel).css('border', '1px');
			$(debugPanel).css('z-index', 2000);
			$(debugPanel).appendTo('body');
		}
		$(debugPanel).css('left', posX);
		$(debugPanel).css('top', posY);
		$(debugPanel).html(debugInfoHtml);
	}

	export function addPagerAccessKeys(e): void {
		let KEY = {
			HOME: 36,
			END: 35,
			PAGE_UP: 33,
			PAGE_DOWN: 34
		};
		if($('div.ui-dp-btns').size() > 0) {
			if(e.altKey) {
				if(e.keyCode == KEY.HOME) {
					$("div.ui-dp-btns > a:nth-child(1)").click();
				} else if(e.keyCode == KEY.PAGE_UP) {
					$("div.ui-dp-btns > a:nth-child(2)").click();
				} else if(e.keyCode == KEY.PAGE_DOWN) {
					$("div.ui-dp-btns > a:nth-child(3)").click();
				} else if(e.keyCode == KEY.END) {
					$("div.ui-dp-btns > a:nth-child(4)").click();
				}
			}
		}
	}


}

