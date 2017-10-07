import jQuery from "jquery";

(function($) {
	if($.browser.msie && $.browser.majorVersion < 10) {
		$.dbg = function(a,b,c,d,e) {
			if(window.console == undefined)
				return;
			switch(arguments.length) {
				default:
					window.console.log(a);
					return;
				case 2:
					window.console.log(a,b);
					return;
				case 3:
					window.console.log(a,b,c);
					return;
				case 4:
					window.console.log(a,b,c,d);
					return;
				case 5:
					window.console.log(a,b,c,d,e);
					return;
			}
		};
	} else if(window.console != undefined) {
		if(window.console.debug != undefined) {
			$.dbg = function() {
				window.console.debug.apply(window.console, arguments);
			};
		} else if(window.console.log != undefined) {
			$.dbg = function() {
				window.console.log.apply(window.console, arguments);
			};
		}
	} else {
		$.dbg = function() {};
	}
})(jQuery);
