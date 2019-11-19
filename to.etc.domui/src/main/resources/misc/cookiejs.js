function onCookieNonsense() {
	${cookieAcceptance}
}

var cookieConsentOptions = {
	title: ${title},
	message: ${msg},
	delay: 600,
	expires: 1,
	link: '${link}',
	onAccept: function(){
		var myPreferences = $.fn.ihavecookies.cookie();
		onCookieNonsense();
		console.log('Yay! The following preferences were saved...');
		console.log(myPreferences);
	},
	uncheckBoxes: true,
	acceptBtnLabel: ${acclabel},
	moreInfoLabel: ${infolabel},
	cookieTypesTitle: 'Select which cookies you want to accept',
	fixedCookieTypeLabel: 'Essential',
	fixedCookieTypeDesc: 'These are essential for the website to work correctly.',
	cookieTypes: ${cookieTypes}
};

$(document).ready(function() {
	$('body').ihavecookies(cookieConsentOptions);
	onCookieNonsense();
});
