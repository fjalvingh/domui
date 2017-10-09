declare namespace WebUI {
	// ajax
	export function clicked(h, id : string, evt: any);
	function getInputFields(fields: object): object;
	function cancelPolling(): void;
	function handleError(request, status, exc): boolean;
	function handleResponse(data, state): void;
	function scall(id: string, action: string, fields? : any) : void;
	function valuechanged(unknown : string, id: string) : void;
	function beforeUnload() : void;

	// busy
	function blockUI() : void;
	function unblockUI() : void;
	function isUIBlocked() : boolean;

	//-- util
	function log(...args): void;
	function refreshElement(id: string) : void;
	function blockUI() : void;
	export function getPostURL() : string;
	let _T: any;
	function format(message: string, ...rest) : string;
	function getPostURL() : string;
	function getObituaryURL(): string;

	let _hideExpiredMessage: boolean;
	function toClip(value: any): void;
	function isReallyIE7() : boolean;
	function isIE8orIE8c() : boolean;
	function isIE8orNewer() : boolean;
	function isNormalIE9plus() : boolean;
	function nearestID(elem: HTMLElement) : any;
	function normalizeKey(evt: any): number;
	function stretchHeightOnNode(elem : HTMLElement) : void;
	function truncateUtfBytes(str: string, nbytes: number) : number;
	function utf8Length(str: string) : number;
	function preventSelection() : boolean;

	function getAbsolutePosition(obj): WebUI.Point;
	function findParentOfTagName(node: any, type: string): any;

	// handler
	function doCustomUpdates(): void;
	function addPagerAccessKeys(e): void;
	function onDocumentReady(): void;
	function onWindowResize() : void;

	// dateinput
	function handleCalendarChanges() : void;

	// resize
	function notifySizePositionChangedOnId(elemId : string) : void;

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
