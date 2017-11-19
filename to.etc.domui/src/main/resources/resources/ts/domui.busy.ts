/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />
namespace WebUI {
	var _busyCount = 0;

	var _busyOvl: any;

	var _busyTimer: number;

	/*
	 * Block the UI while an AJAX call is in progress.
	 */
	export function blockUI() : void {
//		console.debug('block, busy=', WebUI._busyCount);
		if(_busyCount++ > 0)
			return;
		var el = document.body;
		if(! el)
			return;
		el.style.cursor = "wait";

		//-- Create a backdrop div sized 100% overlaying the body, initially just fully transparant (just blocking mouseclicks).
		var d = document.createElement('div');
		el.appendChild(d);
		d.className = 'ui-io-blk';
		_busyOvl = d;
		_busyTimer = setTimeout(() => busyIndicate(), 250);
	}

	export function isUIBlocked() : boolean {
		return _busyOvl != undefined && _busyOvl != null;
	}

	function busyIndicate() {
		if(_busyTimer) {
			clearTimeout(_busyTimer);
			_busyTimer = null;
		}
		if(_busyOvl) {
			_busyOvl.className = "ui-io-blk2";
		}
	}

	export function unblockUI() {
//		console.debug('unblock, busy=', WebUI._busyCount);
		if(_busyCount <= 0 || ! _busyOvl)
			return;
		if(--_busyCount != 0)
			return;
		if(_busyTimer) {
			clearTimeout(_busyTimer);
			_busyTimer = null;
		}
		var el = document.body;
		if(!el)
			return;

		el.style.cursor = "default";
		try {
			el.removeChild(_busyOvl);
		} catch(x) {
			//-- It can fail when the entire page has been replaced.
		}
		_busyOvl= null;
	}
}
