(function($) {
	$.fn.addIframe = function(iframeHtml) {
		return this.each(function() {

			var srcMatch = iframeHtml.match(/src=["']([^"']+)["']/);
			var styleMatch = iframeHtml.match(/style=["']([^"']+)["']/);

			if (styleMatch) {
				var width = 600;
				var height = 400;
				var styles = styleMatch[1].split(';');
				styles.forEach(style => {
					var [property, value] = style.split(':').map(s => s.trim());
					if (property === 'width') {
						width = value;
					} else if (property === 'height') {
						height = value;
					}
				});
			}

			let newIframe = $("<iframe>", {
				src: srcMatch[1],
				width: width,
				height: height,
				frameborder: "0"
			}).css({
				width: width,
				height: height
			});

			$(this).append(newIframe);
		});
	};
})(jQuery);

