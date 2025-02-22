(function($) {
	$.fn.addIframe = function(iframeHtml) {
		return this.each(function() {

			var tempDiv = document.createElement('div');
			var newIframe = document.createElement('iframe');

			var srcMatch = iframeHtml.match(/src=["']([^"']+)["']/);
			var styleMatch = iframeHtml.match(/style=["']([^"']+)["']/);

			if (srcMatch) {
				newIframe.src = srcMatch[1];
			}

			if (styleMatch) {
				var styles = styleMatch[1].split(';');
				styles.forEach(style => {
					var [property, value] = style.split(':').map(s => s.trim());
					if (property === 'width') {
						newIframe.width = value;
					} else if (property === 'height') {
						newIframe.height = value;
					}
				});
			} else {
				// Default width en height als ze niet zijn opgegeven
				newIframe.width = '600';
				newIframe.height = '400';
			}
			tempDiv.appendChild(newIframe);
			$(this).before(tempDiv);
		});
	};
})(jQuery);

