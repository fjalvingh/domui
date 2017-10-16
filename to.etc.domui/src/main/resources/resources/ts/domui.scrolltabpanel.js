var WebUI;
(function (WebUI) {
    var _ignoreScrollClick = 0;
    function scrollLeft(bLeft) {
        if (this._ignoreScrollClick != 0 || $(bLeft).hasClass('ui-stab-dis'))
            return;
        var scrlNavig = $(bLeft.parentNode);
        var offset = -1 * parseInt($('ul', scrlNavig).css('marginLeft'));
        var diff = $(scrlNavig).width() - 2 * $('.ui-stab-scrl-left', scrlNavig).width();
        var me = this;
        var disa = false;
        if (diff >= offset) {
            disa = true;
            diff = offset;
        }
        this._ignoreScrollClick++;
        $('ul', scrlNavig).animate({ marginLeft: '+=' + diff }, 400, 'swing', function () {
            $('.ui-stab-scrl-right', scrlNavig).removeClass('ui-stab-dis');
            if (disa) {
                $(bLeft).addClass('ui-stab-dis');
            }
            me._ignoreScrollClick--;
        });
    }
    function scrollRight(bRight) {
        if (this._ignoreScrollClick != 0 || $(bRight).hasClass('ui-stab-dis'))
            return;
        var scrlNavig = $(bRight.parentNode);
        var tabsTotalWidth = $('li:last', scrlNavig).width() + 8 + $('li:last', scrlNavig).offset().left - $('li:first', scrlNavig).offset().left;
        var tabsVisibleWidth = $(scrlNavig).width() - 2 * $('.ui-stab-scrl-right', scrlNavig).width();
        var maxLeftOffset = tabsTotalWidth - tabsVisibleWidth;
        var diff = tabsVisibleWidth;
        var offset = -1 * parseInt($('ul', scrlNavig).css('marginLeft'));
        var disa = false;
        if (offset >= maxLeftOffset) {
            return;
        }
        else if (diff + offset >= maxLeftOffset) {
            diff = maxLeftOffset - offset;
            disa = true;
        }
        this._ignoreScrollClick++;
        var me = this;
        $('ul', scrlNavig).animate({ marginLeft: '-=' + diff }, 400, 'swing', function () {
            $('.ui-stab-scrl-left', scrlNavig).removeClass('ui-stab-dis');
            if (disa) {
                $(bRight).addClass('ui-stab-dis');
            }
            me._ignoreScrollClick--;
        });
    }
    function recalculateScrollers(scrlNavigId) {
        var scrlNavig = document.getElementById(scrlNavigId);
        var tabsTotalWidth = $('li:last', scrlNavig).width() + 8 + $('li:last', scrlNavig).offset().left - $('li:first', scrlNavig).offset().left;
        var tabsVisibleWidth = $(scrlNavig).width() - 2 * $('.ui-stab-scrl-right', scrlNavig).width();
        if (tabsTotalWidth > tabsVisibleWidth) {
            var leftM = parseInt($('ul', scrlNavig).css('marginLeft'));
            if (tabsTotalWidth + leftM > tabsVisibleWidth) {
                $('.ui-stab-scrl-right', scrlNavig).removeClass('ui-stab-dis');
            }
            else {
                $('.ui-stab-scrl-right', scrlNavig).addClass('ui-stab-dis');
            }
            if (leftM < 0) {
                $('.ui-stab-scrl-left', scrlNavig).removeClass('ui-stab-dis');
            }
            else {
                $('.ui-stab-scrl-left', scrlNavig).addClass('ui-stab-dis');
            }
        }
        else {
            $('.ui-stab-scrl-left', scrlNavig).css('display', 'none');
            $('.ui-stab-scrl-right', scrlNavig).css('display', 'none');
            $('ul', scrlNavig).animate({ marginLeft: 0 }, 400, 'swing');
        }
    }
})(WebUI || (WebUI = {}));
//# sourceMappingURL=domui.scrolltabpanel.js.map