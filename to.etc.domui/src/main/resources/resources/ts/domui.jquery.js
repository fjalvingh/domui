(function ($) {
    if ($.browser.msie && $.browser.majorVersion < 10) {
        $.dbg = function (a, b, c, d, e) {
            if (window.console == undefined)
                return;
            switch (arguments.length) {
                default:
                    window.console.log(a);
                    return;
                case 2:
                    window.console.log(a, b);
                    return;
                case 3:
                    window.console.log(a, b, c);
                    return;
                case 4:
                    window.console.log(a, b, c, d);
                    return;
                case 5:
                    window.console.log(a, b, c, d, e);
                    return;
            }
        };
    }
    else if (window.console != undefined) {
        if (window.console.debug != undefined) {
            $.dbg = function () {
                window.console.debug.apply(window.console, arguments);
            };
        }
        else if (window.console.log != undefined) {
            $.dbg = function () {
                window.console.log.apply(window.console, arguments);
            };
        }
    }
    else {
        $.dbg = function () { };
    }
})(jQuery);
$.fn.extend({
    center: function () {
        if (this.css("position") != "fixed") {
            this.css("position", "absolute");
            this.css("top", Math.max(0, (($(window).height() - this.outerHeight()) / 2) + $(window).scrollTop()) + "px");
            this.css("left", Math.max(0, (($(window).width() - this.outerWidth()) / 2) + $(window).scrollLeft()) + "px");
        }
        else {
            this.css("top", Math.max(0, (($(window).height() - this.outerHeight()) / 2)) + "px");
            this.css("left", Math.max(0, (($(window).width() - this.outerWidth()) / 2)) + "px");
        }
        return this;
    },
    cookie: function (name, value, options) {
        if (value !== undefined) {
            if (value === null)
                options.expires = -1;
            if (typeof options.expires === 'number') {
                var dt = new Date();
                dt.setDate(dt.getDate() + options.expires);
                options.expires = dt;
            }
            value = String(value);
            var c = [
                encodeURIComponent(name), '=', encodeURIComponent(value),
                options.expires ? '; expires=' + options.expires.toUTCString() : '',
                options.path ? '; path=' + options.path : '',
                options.domain ? '; domain=' + options.domain : '',
                options.secure ? '; secure' : ''
            ].join('');
            return (document.cookie = c);
        }
        var cookar = document.cookie.split("; ");
        for (var i = cookar.length; --i >= 0;) {
            var par = cookar[i].split('=');
            if (par.length < 2)
                continue;
            var rname = decodeURIComponent(par.shift().replace(/\+/g, ' '));
            if (rname === name) {
                return decodeURIComponent(par.join('=').replace(/\+/g, ' '));
            }
        }
        return null;
    },
    fixOverflow: function () {
        if (!$.browser.msie || $.browser.version.substring(0, 1) != "7")
            return this;
        return this.each(function () {
            if (this.scrollWidth > this.offsetWidth) {
                $(this).css({ 'padding-bottom': '20px' });
                if (this.scrollHeight <= this.offsetHeight) {
                    $(this).css({ 'overflow-y': 'hidden' });
                }
            }
            if (this.scrollHeight > this.offsetHeight) {
                $(this).css({ 'margin-right': '17px' });
                if (this.scrollWidth <= this.offsetWidth) {
                    $(this).css({ 'overflow-x': 'hidden' });
                }
            }
        });
    },
    doStretch: function () {
        return this.each(function () {
            WebUI.stretchHeightOnNode(this);
        });
    },
    setBackgroundImageMarker: function () {
        return this.each(function () {
            if ($(this).markerTransformed) {
                return;
            }
            var imageUrl = 'url(' + $(this).attr('marker') + ')';
            var value = $(this).val();
            try {
                if ((!(this == document.activeElement)) && value.length == 0) {
                    $(this).css('background-image', imageUrl);
                }
            }
            catch (e) {
            }
            $(this).css('background-repeat', 'no-repeat');
            $(this).bind('focus', function (e) {
                $(this).css('background-image', 'none');
            });
            $(this).bind('blur', function (e) {
                if (value.length == 0) {
                    $(this).css('background-image', imageUrl);
                }
                else {
                    $(this).css('background-image', 'none');
                }
            });
            $(this).markerTransformed = true;
        });
    }
});
//# sourceMappingURL=domui.jquery.js.map