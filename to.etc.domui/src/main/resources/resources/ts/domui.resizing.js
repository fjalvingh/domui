var WebUI;
(function (WebUI) {
    function autoHeightReset(topid, flexid, bottom) {
        $(window).bind("resize", function () {
            recalculateAutoHeight(topid, flexid, bottom);
        });
        recalculateAutoHeight(topid, flexid, bottom);
    }
    function recalculateAutoHeight(topid, flexid, bottom) {
        try {
            var tbot = $(topid).offset().top + $(topid).height();
            var height = $(window).height() - tbot - bottom;
            $(flexid).height(height + "px");
        }
        catch (x) {
        }
    }
    function setThreePanelHeight(top, middle, bottom) {
        middle = "#" + middle;
        var height = $(middle).parent().height();
        var theight = 0;
        if (typeof top === "string") {
            theight = $("#" + top).height();
        }
        else if (top) {
            theight = top;
        }
        var bheight = 0;
        if (typeof bottom === "string") {
            bheight = $("#" + bottom).height();
        }
        else if (bottom) {
            bheight = $(bottom).height();
        }
        height -= theight - bheight;
        if (height < 0) {
            height = 0;
        }
        $(middle).height(height + "px");
        $(middle).css({ "overflow-y": "auto" });
    }
    function autoHeight(flexid, bottom) {
        $(window).bind("resize", function () {
            autoHeightRecalc(flexid, bottom);
        });
        autoHeightRecalc(flexid, bottom);
    }
    WebUI.autoHeight = autoHeight;
    function autoHeightRecalc(flexid, bottom) {
        var tbot = $(flexid).offset().top;
        var height = $(window).height() - tbot - bottom;
        $(flexid).height(height + "px");
    }
    WebUI.autoHeightRecalc = autoHeightRecalc;
    function notifySizePositionChangedOnId(elemId) {
        var element = document.getElementById(elemId);
        if (!element) {
            return;
        }
        var fields = {};
        fields[element.id + "_rect"] = $(element).position().left + "," + $(element).position().top + "," + $(element).width() + "," + $(element).height();
        fields["window_size"] = window.innerWidth + "," + window.innerHeight;
        WebUI.scall(element.id, "notifyClientPositionAndSize", fields);
    }
    WebUI.notifySizePositionChangedOnId = notifySizePositionChangedOnId;
    function notifySizePositionChanged(event, ui) {
        var element = ui.helper.get(0);
        if (!element) {
            return;
        }
        notifySizePositionChangedOnId(element.id);
    }
    WebUI.notifySizePositionChanged = notifySizePositionChanged;
    function floatingDivResize(ev, ui) {
        $(ui.helper.get(0)).css('position', 'fixed');
        $('[stretch=true]').doStretch();
        $('.ui-dt, .ui-fixovfl').fixOverflow();
    }
    WebUI.floatingDivResize = floatingDivResize;
    function onWindowResize() {
        WebUI.doCustomUpdates();
    }
    WebUI.onWindowResize = onWindowResize;
})(WebUI || (WebUI = {}));
//# sourceMappingURL=domui.resizing.js.map
