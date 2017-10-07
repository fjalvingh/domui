declare const WebUI: WebUIStatic;

interface WebUIStatic {
	refreshElement(id: string) : void;
	blockUI() : void;
	_T: any;
	_hideExpiredMessage: boolean;
	isNormalIE9plus() : boolean;
}
