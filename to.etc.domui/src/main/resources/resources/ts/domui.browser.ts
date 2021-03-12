/// <reference types="./node_modules/@types/jquery" />
/// <reference types="./node_modules/@types/jqueryui" />
/// <reference path="domui.jquery.d.ts" />
namespace WebUI {
	//Returns T if browser is really using IE7 rendering engine (since IE8 compatibility mode presents  browser as version 7 but renders as IE8!)
	export function isReallyIE7() : boolean {
		return false;
	}

	//Returns T if browser is IE8 or IE8 compatibility mode
	export function isIE8orIE8c() : boolean {
		return false;
	}

	//Returns T if browser is IE of at least version 9 and does not run in any of compatibility modes for earlier versions
	export function isNormalIE9plus() : boolean {
		return false;
	}

	//Returns T if browser is IE of at least version 8 even if it runs in IE7 compatibility mode
	export function isIE8orNewer() : boolean {
		return false;
	}


}
