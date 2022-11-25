/// <reference types="jquery" />
/// <reference types="jqueryui" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />
//import WebUI from "domui.webui.util";
$(function(){
	$.getScript(window['DomUIappURL'] + "$js/domui-date-checker.js");
});

function _block() : void {
	WebUI.blockUI();
}
function _unblock() : void {
	WebUI.unblockUI();
}

$(document).ajaxStart(_block).ajaxStop(_unblock);
$(window).bind('beforeunload', function() {
	WebUI.beforeUnload();
	return undefined;
});
$(window).resize(function() {
	WebUI.propagateResize();
});

//-- Embedded $.browser support - doing feature detection is fine except in the presence of specific browser bugs.

(function ($) {
	if (!$.browser && 1.9 <= parseFloat($.fn.jquery)) {
		let a = {ua: "", webkit: false, msie: false, mobile: "", browserArray: [], browser: "", version: 1.0};
		navigator && navigator.userAgent && (a.ua = navigator.userAgent, a.webkit = /WebKit/i.test(a.ua), a.browserArray = "MSIE Chrome Opera Kindle Silk BlackBerry PlayBook Android Safari Mozilla Nokia".split(" "), /Sony[^ ]*/i.test(a.ua) ? a.mobile = "Sony" : /RIM Tablet/i.test(a.ua) ? a.mobile = "RIM Tablet" : /BlackBerry/i.test(a.ua) ? a.mobile = "BlackBerry" : /iPhone/i.test(a.ua) ? a.mobile = "iPhone" : /iPad/i.test(a.ua) ? a.mobile = "iPad" : /iPod/i.test(a.ua) ? a.mobile = "iPod" : /Opera Mini/i.test(a.ua) ? a.mobile = "Opera Mini" : /IEMobile/i.test(a.ua) ? a.mobile = "IEMobile" : /BB[0-9]{1,}; Touch/i.test(a.ua) ? a.mobile = "BlackBerry" : /Nokia/i.test(a.ua) ? a.mobile = "Nokia" : /Android/i.test(a.ua) && (a.mobile = "Android"), /MSIE|Trident/i.test(a.ua) ? (a.browser = "MSIE", a.version = /MSIE/i.test(navigator.userAgent) && 0 < parseFloat(a.ua.split("MSIE")[1].replace(/[^0-9\.]/g, "")) ? parseFloat(a.ua.split("MSIE")[1].replace(/[^0-9\.]/g, "")) : 99.99, /Trident/i.test(a.ua) && /rv:([0-9]{1,}[\.0-9]{0,})/.test(a.ua) && (a.version = parseFloat(a.ua.match(/rv:([0-9]{1,}[\.0-9]{0,})/)[1].replace(/[^0-9\.]/g, "")))) : /Chrome/.test(a.ua) ? (a.browser = "Chrome", a.version = parseFloat(a.ua.split("Chrome/")[1].split("Safari")[0].replace(/[^0-9\.]/g, ""))) : /Opera/.test(a.ua) ? (a.browser = "Opera", a.version = parseFloat(a.ua.split("Version/")[1].replace(/[^0-9\.]/g, ""))) : /Kindle|Silk|KFTT|KFOT|KFJWA|KFJWI|KFSOWI|KFTHWA|KFTHWI|KFAPWA|KFAPWI/i.test(a.ua) ? (a.mobile = "Kindle", /Silk/i.test(a.ua) ? (a.browser = "Silk", a.version = parseFloat(a.ua.split("Silk/")[1].split("Safari")[0].replace(/[^0-9\.]/g, ""))) : /Kindle/i.test(a.ua) && /Version/i.test(a.ua) && (a.browser = "Kindle", a.version = parseFloat(a.ua.split("Version/")[1].split("Safari")[0].replace(/[^0-9\.]/g, "")))) : /BlackBerry/.test(a.ua) ? (a.browser = "BlackBerry", a.version = parseFloat(a.ua.split("/")[1].replace(/[^0-9\.]/g, ""))) : /PlayBook/.test(a.ua) ? (a.browser = "PlayBook", a.version = parseFloat(a.ua.split("Version/")[1].split("Safari")[0].replace(/[^0-9\.]/g, ""))) : /BB[0-9]{1,}; Touch/.test(a.ua) ? (a.browser = "Blackberry", a.version = parseFloat(a.ua.split("Version/")[1].split("Safari")[0].replace(/[^0-9\.]/g, ""))) : /Android/.test(a.ua) ? (a.browser = "Android", a.version = parseFloat(a.ua.split("Version/")[1].split("Safari")[0].replace(/[^0-9\.]/g, ""))) : /Safari/.test(a.ua) ? (a.browser = "Safari", a.version = parseFloat(a.ua.split("Version/")[1].split("Safari")[0].replace(/[^0-9\.]/g, ""))) : /Firefox/.test(a.ua) ? (a.browser = "Mozilla", a.version = parseFloat(a.ua.split("Firefox/")[1].replace(/[^0-9\.]/g, ""))) : /Nokia/.test(a.ua) && (a.browser = "Nokia", a.version = parseFloat(a.ua.split("Browser")[1].replace(/[^0-9\.]/g, ""))));
		if (a.browser) for (var b in a.browserArray) a[a.browserArray[b].toLowerCase()] = a.browser == a.browserArray[b];
		$.extend(!0, $.browser = {}, a)
	}
})(jQuery);

//-- calculate browser major and minor versions
{



	try {
		var v = $.browser.version.split(".");
		$.browser.majorVersion = parseInt(v[0], 10);
		$.browser.minorVersion = parseInt(v[1], 10);
		$.browser.msie = false;

		//-- And like clockwork MS fucks up with IE 11: it no longer registers as msie. Fix that here.
		// if(navigator.appName == 'Netscape') {
		// 	var ua = navigator.userAgent;
		// 	if(ua.indexOf("Trident/") != -1)
		// 		$.browser.msie = true;
		// }

		if (/Edge/.test(navigator.userAgent)) {
			$.browser.ieedge = true;
		}
	} catch(x) {}

//	alert('bmaj='+$.browser.majorVersion+", mv="+$.browser.minorVersion);
}

var DomUI = WebUI;

$(document).ready(WebUI.onDocumentReady);
$(window).resize(WebUI.onWindowResize);
$(document).ajaxComplete( function() {
	WebUI.handleCalendarChanges();
	WebUI.doCustomUpdates();
});

$(document).keydown(function(e){
	WebUI.addPagerAccessKeys(e);
	WebUI.addDropDownPickerKeys(e);
});

