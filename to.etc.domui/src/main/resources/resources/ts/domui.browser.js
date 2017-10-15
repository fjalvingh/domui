var WebUI;
(function (WebUI) {
    function isReallyIE7() {
        var doc = document;
        return ($.browser.msie && parseInt($.browser.version) == 7 && (!doc.documentMode || doc.documentMode == 7));
    }
    WebUI.isReallyIE7 = isReallyIE7;
    function isIE8orIE8c() {
        var doc = document;
        return ($.browser.msie && (parseInt($.browser.version) == 8 || (parseInt($.browser.version) == 7 && doc.documentMode == 8)));
    }
    WebUI.isIE8orIE8c = isIE8orIE8c;
    function isNormalIE9plus() {
        var doc = document;
        return ($.browser.msie && parseInt($.browser.version) >= 9 && doc.documentMode >= 9);
    }
    WebUI.isNormalIE9plus = isNormalIE9plus;
    function isIE8orNewer() {
        var doc = document;
        return ($.browser.msie && (parseInt($.browser.version) >= 8 || (parseInt($.browser.version) == 7 && doc.documentMode >= 8)));
    }
    WebUI.isIE8orNewer = isIE8orNewer;
})(WebUI || (WebUI = {}));
//# sourceMappingURL=domui.browser.js.map