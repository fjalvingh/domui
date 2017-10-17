/// <reference path="typings/jquery/jquery.d.ts" />
/// <reference path="domui.jquery.d.ts" />
/// <reference path="domui.webui.ts" />
//import WebUI from "domui.webui.util";
$(function () {
    $.getScript("$js/domui-date-checker.js");
});
function _block() {
    WebUI.blockUI();
}
function _unblock() {
    WebUI.unblockUI();
}
$(document).ajaxStart(_block).ajaxStop(_unblock);
$(window).bind('beforeunload', function () {
    WebUI.beforeUnload();
    return undefined;
});
//-- calculate browser major and minor versions
{
    try {
        var v = $.browser.version.split(".");
        $.browser.majorVersion = parseInt(v[0], 10);
        $.browser.minorVersion = parseInt(v[1], 10);
        //-- And like clockwork MS fucks up with IE 11: it no longer registers as msie. Fix that here.
        if (navigator.appName == 'Netscape') {
            var ua = navigator.userAgent;
            if (ua.indexOf("Trident/") != -1)
                $.browser.msie = true;
        }
        if (/Edge/.test(navigator.userAgent)) {
            $.browser.ieedge = true;
        }
    }
    catch (x) { }
    //	alert('bmaj='+$.browser.majorVersion+", mv="+$.browser.minorVersion);
}
var DomUI = WebUI;
$(document).ready(WebUI.onDocumentReady);
$(window).resize(WebUI.onWindowResize);
$(document).ajaxComplete(function () {
    WebUI.handleCalendarChanges();
    WebUI.doCustomUpdates();
});
$(document).keydown(function (e) {
    WebUI.addPagerAccessKeys(e);
});
