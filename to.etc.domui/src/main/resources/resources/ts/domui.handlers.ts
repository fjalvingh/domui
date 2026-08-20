/// <reference types="jquery" />
/// <reference types="jqueryui" />
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
			function (index, node) {
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
			.bind('input.domui propertychange.domui', function () {
				let maxLength = attrNumber(this, 'mxlength');				// Use mxlength because Chrome improperly implements maxlength (issue 252613)
				let maxBytes = attrNumber(this, 'maxbytes');
				let val = $(this).val() as string;
				let newlines = (val.match(/\r\n/g) || []).length;				// Count the #of 2-char newlines, as they will be replaced by 1 newline character
				if (maxBytes < 0) {
					if (maxLength < 0)
						return;
				} else if (maxLength < 0) {
					maxLength = maxBytes;
				}

				if (val.length + newlines > maxLength) {
					val = val.substring(0, maxLength - newlines);
					$(this).val(val);
				}
				if (maxBytes > 0) {
					let cutoff = WebUI.truncateUtfBytes(val, maxBytes);
					if (cutoff < val.length) {
						val = val.substring(0, cutoff);
						$(this).val(val);
					}
				}
			});

		//-- Limit textarea size on key presses
		$("textarea[mxlength], textarea[maxbytes]")
			.unbind("keypress.domui")
			.bind('keypress.domui', function (evt) {
				if (evt.which == 0 || evt.which == 8)
					return true;

				//-- Is the thing too long already?
				let maxLength = attrNumber(this, 'mxlength');				// Use mxlength because Chrome improperly implements maxlength (issue 252613)
				let maxBytes = attrNumber(this, 'maxbytes');
				let val = $(this).val() as string;
				let newlines = (val.match(/\r\n/g) || []).length;				// Count the #of 2-char newlines, as they will be replaced by 1 newline character
				if (maxBytes < 0) {
					if (maxLength < 0)
						return true;
				} else if (maxLength < 0) {
					maxLength = maxBytes;
				}
				if (val.length - newlines >= maxLength)							// Too many chars -> not allowed
					return false;
				if (maxBytes > 0) {
					let bytes = WebUI.utf8Length(val);
					if (bytes >= maxBytes)
						return false;
				}
				return true;
			});

		//custom updates may fire several times in sequence, se we fire custom contributors only after it gets steady for a while (500ms)
		if (_customUpdatesContributorsTimerID) {
			window.clearTimeout(_customUpdatesContributorsTimerID);
			_customUpdatesContributorsTimerID = null;
		}
		_customUpdatesContributorsTimerID = window.setTimeout(function () {
			try {
				_customUpdatesContributors.fire()
			} catch (ex) {
			}
		}, 500);
		//$('.ui-dt-ovflw-tbl').floatThead('reflow');
	}

	function attrNumber(elem, name: string): number {
		let val = $(elem).attr(name);
		if (typeof val == 'undefined')
			return -1;
		return Number(val);
	}

	export function onDocumentReady(): void {
		checkBrowser();
		checkPasswordManagerInterference();
		WebUI.handleCalendarChanges();
		if ((window as any).DomUIDevel)
			handleDevelopmentMode();
		doCustomUpdates();
	}

	/**
	 * Marker nodes LastPass injects into the page when it decorates a field with its in-field
	 * icon. These are LastPass internals, not a documented contract: if they are renamed the
	 * detection below silently stops working, which is the acceptable failure mode here.
	 */
	const PWMGR_MARKER = "[data-lastpass-icon-root],[data-lastpass-root]";

	/** Set once the user dismissed the warning, so we nag at most once per browser. */
	const PWMGR_DISMISSED = "domui.pwmgrWarningDismissed";

	/** How often to look for the marker once the user touched something. */
	const PWMGR_PROBE_INTERVAL = 500;

	/** Give up after this many probes, so we never keep looking for the rest of the session. */
	const PWMGR_MAX_PROBES = 40;

	/**
	 * LastPass ignores every documented opt-out attribute we render on our inputs (see
	 * HtmlTagRenderer#renderPasswordManagerHints) and still draws its icon inside the focused
	 * field, where it covers the field's content. Its settings cannot be read from a page, so
	 * instead we detect the symptom: the marker node it injects next to the field. Nothing at
	 * all is observable before the user touches a field - LastPass leaves no trace on load and
	 * ignores programmatic focus - so this cannot be decided any earlier than this.
	 *
	 * Deliberately silent when the marker is absent: a missed warning is harmless, a warning
	 * shown to someone without the problem is not.
	 */
	function checkPasswordManagerInterference(): void {
		if (window.localStorage.getItem(PWMGR_DISMISSED) === "true")
			return;

		let probes = 0;
		let timer: number = null;

		function stopProbing(): void {
			if (timer !== null) {
				window.clearInterval(timer);
				timer = null;
			}
			document.removeEventListener("focusin", onInteraction);
			document.removeEventListener("click", onInteraction);
		}

		function probe(): void {
			if (document.querySelector(PWMGR_MARKER)) {
				stopProbing();
				showPasswordManagerWarning();
			} else if (++probes >= PWMGR_MAX_PROBES) {
				stopProbing();								// No LastPass, or its icons are off.
			}
		}

		/*
		 * Measured on Chrome: LastPass injects its marker around two seconds after the field is
		 * touched, and that delay is nothing we control - hence a bounded poll instead of a
		 * single check on a guessed delay. Both events are needed to start it: clicking a field
		 * that already has focus (DomUI autofocuses one on every page) fires no focusin, while
		 * tabbing into a field fires no click.
		 */
		function onInteraction(): void {
			if (timer === null)
				timer = window.setInterval(probe, PWMGR_PROBE_INTERVAL);
		}

		document.addEventListener("focusin", onInteraction);
		document.addEventListener("click", onInteraction);
	}

	/**
	 * Show the dismissible banner telling the user to switch their password manager's in-field
	 * icons off. Styled inline on purpose: this is a framework level warning that must render
	 * in every theme, including themes outside this repository that never heard of its class.
	 */
	function showPasswordManagerWarning(): void {
		if (document.getElementById("domui-pwmgr-warning"))
			return;

		let bar = document.createElement("div");
		bar.id = "domui-pwmgr-warning";
		bar.setAttribute("role", "alert");
		bar.style.cssText = "position:fixed;left:0;right:0;bottom:0;z-index:20000;display:flex;"
			+ "align-items:center;gap:12px;padding:12px 16px;background:#b3261e;color:#fff;"
			+ "border-top:1px solid #7f1a15;font-size:13px;font-weight:600;line-height:1.4;"
			+ "box-shadow:0 -2px 8px rgba(0,0,0,.3)";

		let msg = document.createElement("span");
		msg.style.cssText = "flex:1";
		msg.appendChild(document.createTextNode(WebUI._T.pwmgrIconWarning));
		bar.appendChild(msg);

		let btn = document.createElement("button");
		btn.type = "button";
		btn.style.cssText = "flex:none;cursor:pointer;padding:4px 12px;border:1px solid #fff;"
			+ "border-radius:3px;background:#fff;color:#b3261e;font-size:inherit;"
			+ "font-weight:inherit";
		btn.appendChild(document.createTextNode(WebUI._T.pwmgrIconDismiss));
		btn.onclick = function (): void {
			window.localStorage.setItem(PWMGR_DISMISSED, "true");
			$(bar).remove();
		};
		bar.appendChild(btn);

		document.body.appendChild(bar);
	}

	function checkBrowser(): void {
		if (this._browserChecked)
			return;
		this._browserChecked = true;

		// //-- We do not support IE 7 and lower anymore.
		// if($.browser.msie && $.browser.majorVersion < 8) {
		// 	//-- Did we already report that warning this session?
		// 	if($.cookie("domuiie") == null) {
		// 		alert(WebUI.format(WebUI._T.sysUnsupported, $.browser.majorVersion));
		// 		$.cookie("domuiie", "true", {});
		// 	}
		// }
	}

	let _debugLastKeypress: number;
	let _debugMouseTarget: HTMLElement;

	export function handleDevelopmentMode(): void {
		// $(document).bind("keypress", function(e) {
		// 	console.log("keypress", e);
		// });

		$(document).bind("keydown", function (e) {
			let action = '';
			// console.log("key: shiftKey=" + e.shiftKey + " ctrl=" + e.ctrlKey + " alt=" + e.altKey + " key=" + e.key);

			if((e.key == '~' || e.key == '`') && e.shiftKey && ! e.altKey && ! e.ctrlKey) {		// Tilde
				action = 'DEVTREE';
			} else if((e.key == '~' || e.key == '`') && e.ctrlKey && e.shiftKey && ! e.altKey) {
				action = 'TESTGEN';
			} else {
				_debugLastKeypress = 0;
				return;
			}

			var t = new Date().getTime();
			if (!_debugLastKeypress || (t - _debugLastKeypress) > 250) {
				_debugLastKeypress = t;
				return;
			}
			var id = WebUI.nearestID(_debugMouseTarget);
			if (!id) {
				id = document.body.id;
			}
			WebUI.scall(id, action, {});
		});
		$(document.body).bind("mousemove", function (e) {
//			if(WebUI._NOMOVE)
//				return;
//			console.debug("move ", e);
			_debugMouseTarget = e.target as HTMLElement; // e.srcElement || e.originalTarget;
		});
	}

	/** *************** Debug thingy - it can be used internaly for debuging javascript ;) ************** */
	export function debug(debugId: string, posX: number, posY: number, debugInfoHtml: any) {
		//Be aware that debugId must not start with digit when using FF! Just lost 1 hour to learn this...
		if ("0123456789".indexOf(debugId.charAt(0)) > -1) {
			alert("debugId(" + debugId + ") starts with digit! Please use different one!");
		}
		let debugPanel = document.getElementById(debugId);
		if (null == debugPanel) {
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
		if ($('div.ui-dp-btns').size() > 0) {
			if (e.altKey) {
				if (e.keyCode == KEY.HOME) {
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

	export function addDropDownPickerKeys(e): void {
		let KEY = {
			ESC: 27
		};
		let visibleDropDownPicker = $('select.ddp-cmb:visible').first();
		if(visibleDropDownPicker.size() === 1) {
			if(e.keyCode === KEY.ESC) {
				visibleDropDownPicker.blur();
			}
		}
	}

	let _checkLeavePage = false;
	let _skipLeavePageCheck = false;

	const beforeUnloadListener = (event) => {
		if (_checkLeavePage && !_skipLeavePageCheck) {
			event.preventDefault();
			return event.returnValue = "Are you sure you want to exit?";
		} else {
			delete event['returnValue'];
		}
	};
	window.addEventListener('beforeunload', beforeUnloadListener);

	export function setCheckLeavePage(v): void {
		_checkLeavePage = v;
	}

	export function setSkipLeavePageCheck(v): void {
		_skipLeavePageCheck = v;
	}
}

