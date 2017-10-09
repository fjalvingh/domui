declare const WebUI: WebUIStatic;

interface WebUIStatic {
	// ajax
	clicked(h, id : string, evt: any);
	getInputFields(fields: object): object;
	cancelPolling(): void;
	handleError(request, status, exc): boolean;
	handleResponse(data, state): void;
	scall(id: string, action: string, fields? : any) : void;
	valuechanged(unknown : string, id: string) : void;

	// busy
	blockUI() : void;
	unblockUI() : void;
	isUIBlocked() : boolean;

	//-- util
	log(...args): void;
	refreshElement(id: string) : void;
	blockUI() : void;
	getPostURL() : string;
	_T: any;
	format(message: string, ...rest) : string;
	getPostURL() : string;
	getObituaryURL(): string;

	_hideExpiredMessage: boolean;
	toClip(value: any): void;
	isReallyIE7() : boolean;
	isIE8orIE8c() : boolean;
	isIE8orNewer() : boolean;
	isNormalIE9plus() : boolean;
	nearestID(elem: HTMLElement) : any;
	normalizeKey(evt: any): number;
	stretchHeightOnNode(elem : HTMLElement) : void;
	truncateUtfBytes(str: string, nbytes: number) : number;
	utf8Length(str: string) : number;
	preventSelection() : boolean;

	getAbsolutePosition(obj): WebUIStatic.Point;
	findParentOfTagName(node: any, type: string): any;

	// handler
	doCustomUpdates(): void;

	// dateinput
	handleCalendarChanges() : void;

	// resize
	notifySizePositionChangedOnId(elemId : string) : void;
}

declare namespace WebUIStatic {
	class Point {
		x: number;
		y: number;

		constructor(x, y);
	}

	class Rect {
		bx: number;
		by: number;
		ex: number;
		ey: number;

		constructor(_bx, _by, _ex, _ey);
	}
}
