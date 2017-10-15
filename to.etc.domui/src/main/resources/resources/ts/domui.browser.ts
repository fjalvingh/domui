/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
namespace WebUI {
	//Returns T if browser is really using IE7 rendering engine (since IE8 compatibility mode presents  browser as version 7 but renders as IE8!)
	export function isReallyIE7() : boolean {
		//Stupid IE8 in compatibility mode lies that it is IE7, and renders as IE8! At least we can detect that using document.documentMode (it is 8 in that case)
		//document.documentMode == 7 		 --- IE8 running in IE7 mode
		//document.documentMode == 8 		 --- IE8 running in IE8 mode or IE7 Compatibility mode
		//document.documentMode == undefined --- plain old IE7
		let doc = document as any;
		return ($.browser.msie && parseInt($.browser.version) == 7 && (!doc.documentMode || doc.documentMode == 7));
	}

	//Returns T if browser is IE8 or IE8 compatibility mode
	export function isIE8orIE8c() : boolean {
		//Stupid IE8 in compatibility mode lies that it is IE7, and renders as IE8! At least we can detect that using document.documentMode (it is 8 in that case)
		//document.documentMode == 7 		 --- IE8 running in IE7 mode
		//document.documentMode == 8 		 --- IE8 running in IE8 mode or IE7 Compatibility mode
		//document.documentMode == undefined --- plain old IE7
		let doc = document as any;
		return ($.browser.msie && (parseInt($.browser.version) == 8 || (parseInt($.browser.version) == 7 && doc.documentMode == 8)));
	}

	//Returns T if browser is IE of at least version 9 and does not run in any of compatibility modes for earlier versions
	export function isNormalIE9plus() : boolean {
		let doc = document as any;
		return ($.browser.msie && parseInt($.browser.version) >= 9 && doc.documentMode >= 9);
	}

	//Returns T if browser is IE of at least version 8 even if it runs in IE7 compatibility mode
	export function isIE8orNewer() : boolean {
		let doc = document as any;
		return ($.browser.msie && (parseInt($.browser.version) >= 8 || (parseInt($.browser.version) == 7 && doc.documentMode >= 8)));
	}


}
