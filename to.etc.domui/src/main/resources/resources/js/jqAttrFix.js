(function () {

	var native = jQuery.attr;
	jQuery.attr = function (element, attr, value) {
		if (attr === 'style') {
			resetStyles(element);
			applyStyles(element, value);
		} else {
			native.apply(jQuery, arguments);
		}
	};

	function applyStyles(element, styleString) {
		var styles = styleString.split(';').filter(Boolean);
		styles.forEach(property => {
			let index = property.indexOf(":");
			if (index !== -1) {
				let p = property.substring(0, index).trim(); // Eerste deel vóór ":"
				let v = property.substring(index + 1).trim(); // Rest van de string
				p = p.replace(/-(\w)/g, (match, letter) => letter.toUpperCase());
				element.style[p.trim()] = v.trim();
			}
		});
	}

	function resetStyles(element) {
		var styleList = [].slice.call(element);
		styleList.forEach(function (propertyName) {
			element.style.removeProperty(propertyName);
		});
	}

}());
